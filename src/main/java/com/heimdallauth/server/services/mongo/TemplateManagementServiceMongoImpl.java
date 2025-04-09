package com.heimdallauth.server.services.mongo;

import com.heimdallauth.server.documents.TemplateDocument;
import com.heimdallauth.server.dto.bifrost.CreateEmailTemplateDTO;
import com.heimdallauth.server.exceptions.TemplateAlreadyExists;
import com.heimdallauth.server.exceptions.TemplateNotFound;
import com.heimdallauth.server.models.bifrost.EmailContent;
import com.heimdallauth.server.models.bifrost.MessageHeader;
import com.heimdallauth.server.models.bifrost.Template;
import com.heimdallauth.server.services.TemplateManagementService;
import com.heimdallauth.server.utils.mapper.TemplateMapper;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TemplateManagementServiceMongoImpl implements TemplateManagementService {
    private static final String COLLECTION_TEMPLATES = "templates_collection";
    private final MongoTemplate mongoTemplate;
    private final TemplateMapper templateMapper;

    @Autowired
    public TemplateManagementServiceMongoImpl(MongoTemplate mongoTemplate, TemplateMapper templateMapper) {
        this.mongoTemplate = mongoTemplate;
        this.templateMapper = templateMapper;
    }

    /**
     * Retrieves a template by its ID.
     * @param templateId The ID of the template to retrieve.
     * @return The template matching the given ID.
     * @throws TemplateNotFound if no template is found with the given ID.
     */
    @Override
    public Template getTemplateById(UUID templateId) {
        Optional<TemplateDocument> matchedTemplateById = Optional.ofNullable(this.mongoTemplate.findById(templateId, TemplateDocument.class, COLLECTION_TEMPLATES));
        return matchedTemplateById.map(templateMapper::mapToTemplateModel).orElseThrow(() -> new TemplateNotFound("Template not found"));
    }

    @Override
    public List<Template> getAllTemplatesByTenantId(UUID tenantId) {
        Query query = Query.query(Criteria.where("tenantId").is(tenantId.toString()));
        List<TemplateDocument> matchedTemplateDocuments = this.mongoTemplate.find(query, TemplateDocument.class, COLLECTION_TEMPLATES);
        if (matchedTemplateDocuments.isEmpty()){
            throw new TemplateNotFound("No Templates found for Tenant ID");
        }
        return matchedTemplateDocuments.stream().map(templateMapper::mapToTemplateModel).collect(Collectors.toList());
    }

    /**
     * Retrieves a list of templates by tenant ID and template name.
     * @param tenantId The tenant ID to search for.
     * @param templateName The name of the template to search for.
     * @return A list of templates matching the criteria.
     * @throws TemplateNotFound if no templates are found with the given criteria.
     */
    @Override
    public List<Template> getTemplateByTenantIdAndTemplateName(UUID tenantId, String templateName) {
        Query templateSearchQuery = Query.query(Criteria.where("templateName").is(templateName).and("tenantId").is(tenantId));
        List<TemplateDocument> matchedTemplates = this.mongoTemplate.find(templateSearchQuery, TemplateDocument.class, COLLECTION_TEMPLATES);
        if (matchedTemplates.isEmpty()){
            throw new TemplateNotFound("Template not found");
        }
        return matchedTemplates.stream().map(templateMapper::mapToTemplateModel).collect(Collectors.toList());
    }
    /**
     * Check if a template with the same name already exists under the same tenant ID.
     * @param tenantId The tenant ID to check against.
     * @param templateName The name of the template to check for.
     * @throws TemplateAlreadyExists if a template with the same name already exists under the same tenant ID.
     */
    private void checkForTemplateWithSameNameUnderSameTenantId(UUID tenantId, String templateName){
        List<Template> getTemplatesWithSameNameUnderSameTenant = getTemplateByTenantIdAndTemplateName(tenantId, templateName);
        if (!getTemplatesWithSameNameUnderSameTenant.isEmpty()){
            throw new TemplateAlreadyExists("Template with same name already exists");
        }
    }

    /**
     * Creates a new email template.
     * @param createEmailTemplateDTO The DTO containing the details of the template to create.
     * @return The created template.
     * @throws TemplateAlreadyExists if a template with the same name already exists under the same tenant ID.
     */
    @Override
    public Template createNewTemplate(CreateEmailTemplateDTO createEmailTemplateDTO) {
        try{
            this.checkForTemplateWithSameNameUnderSameTenantId(createEmailTemplateDTO.tenantId(), createEmailTemplateDTO.templateName());
            TemplateDocument documentToSave = TemplateDocument.builder()
                    .id(UUID.randomUUID().toString())
                    .templateName(createEmailTemplateDTO.templateName())
                    .tenantId(createEmailTemplateDTO.tenantId().toString())
                    .content(createEmailTemplateDTO.templatedEmailContent())
                    .defaultMessageHeaders(createEmailTemplateDTO.defaultEmailHeaders())
                    .build();
            this.mongoTemplate.save(documentToSave, COLLECTION_TEMPLATES);
        }catch (TemplateAlreadyExists e){
            log.error("Template with same name already exists");
            throw e;
        }
        return null;
    }

    @Override
    public List<Template> updateTemplate(UUID templateId, CreateEmailTemplateDTO createEmailTemplateDTO) {
        EmailContent updatedEmailContent = createEmailTemplateDTO.templatedEmailContent();
        List<MessageHeader> updatedDefaultEmailHeaders = createEmailTemplateDTO.defaultEmailHeaders();
        Update mongoUpdate = Update.update("content",updatedEmailContent).set("defaultMessageHeaders", updatedDefaultEmailHeaders);
        Query selectionQuery = Query.query(Criteria.where("id").is(templateId.toString()));
        UpdateResult mongoUpdateResult = this.mongoTemplate.updateMulti(selectionQuery, mongoUpdate, TemplateDocument.class, COLLECTION_TEMPLATES);
        log.debug("Updated {} templates with ID {}", mongoUpdateResult.getModifiedCount(), templateId);
        if (mongoUpdateResult.getModifiedCount() == 0) {
            throw new TemplateNotFound("Template not found");
        }else{
            return this.mongoTemplate.find(selectionQuery, TemplateDocument.class, COLLECTION_TEMPLATES)
                    .stream()
                    .map(templateMapper::mapToTemplateModel)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteTemplate(UUID templateId) {
        Query deleteTemplateQuery = Query.query(Criteria.where("id").is(templateId.toString()));
        DeleteResult mongoDeleteResult = this.mongoTemplate.remove(deleteTemplateQuery, TemplateDocument.class, COLLECTION_TEMPLATES);
        log.debug("Deleted {} templates with ID {}", mongoDeleteResult.getDeletedCount(), templateId);
    }
}
