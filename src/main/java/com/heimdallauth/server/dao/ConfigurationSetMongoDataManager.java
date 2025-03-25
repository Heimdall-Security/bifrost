package com.heimdallauth.server.dao;

import com.heimdallauth.server.constants.EmailTemplateAction;
import com.heimdallauth.server.dao.documents.ConfigurationSetMaster;
import com.heimdallauth.server.dao.documents.EmailTemplateDocument;
import com.heimdallauth.server.dao.documents.SuppressionListDocument;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.models.ConfigurationSetModel;
import com.heimdallauth.server.models.EmailTemplateModel;
import com.heimdallauth.server.models.SmtpProperties;
import com.heimdallauth.server.utils.MetadataUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;

import static com.heimdallauth.server.constants.HeimdallBifrostExceptionMessages.CONFIGURATION_SET_NOT_FOUND;
import static com.heimdallauth.server.constants.MongoCollectionNames.*;

@Repository
public class ConfigurationSetMongoDataManager implements ConfigurationSetDataManager {
    private static final ModelMapper CONFIGURATION_SET_MAPPER = new ModelMapper();
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ConfigurationSetMongoDataManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean isConfigurationSetNameExist(String configurationSetName) {
        return false;
    }

    @Override
    public void saveConfigurationSet(UUID tenantId,String configurationSetName, String configurationSetDescription) {
        UUID configurationSetId = UUID.randomUUID();
        ConfigurationSetMaster configurationSetMaster = ConfigurationSetMaster.builder()
                .id(configurationSetId)
                .tenantId(tenantId)
                .configurationSetName(configurationSetName)
                .configurationSetDescription(configurationSetDescription)
                .build();
        List<EmailTemplateDocument> emailTemplates = getEmailTemplates(configurationSetId);
        SuppressionListDocument suppressionListDocument = SuppressionListDocument.builder()
                .id(UUID.randomUUID())
                .configurationSetId(configurationSetId)
                .suppressions(Collections.emptyList())
                .metadata(MetadataUtils.getCreationAndUpdateTimestamps())
                .createdOn(Instant.now())
                .updatedOn(Instant.now())
                .build();
        this.mongoTemplate.save(configurationSetMaster, CONFIGURATION_SET_COLLECTION);
        this.mongoTemplate.save(emailTemplates, EMAILS_TEMPLATE_COLLECTION);
        this.mongoTemplate.save(suppressionListDocument, SUPPRESSION_LIST_COLLECTION);
    }

    @Override
    public ConfigurationSetModel updateConfigurationSet(UUID configurationSetId, ConfigurationSetModel updatedConfigurationSetModel) {
        //TODO: Implement the update logic properly
        Optional<ConfigurationSetModel> existingConfigurationSet = Optional.ofNullable(getConfigurationSetById(configurationSetId));
        existingConfigurationSet.ifPresentOrElse((configurationSetModel) -> {
            List<EmailTemplateModel> emailTemplates = updatedConfigurationSetModel.getTemplates();
            SmtpProperties smtpProperties = updatedConfigurationSetModel.getSmtpProperties();
        }, ()-> {
            throw new ConfigurationSetNotFound(CONFIGURATION_SET_NOT_FOUND);
        });
        return this.getConfigurationSetById(configurationSetId);
    }


    @Override
    public ConfigurationSetModel getConfigurationSetById(UUID configurationSetId) {
        Aggregation aggregationPipelineConfig = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("id").is(configurationSetId)),
                Aggregation.lookup(EMAILS_TEMPLATE_COLLECTION, "id", "configurationSetId", "emailTemplates"),
                Aggregation.lookup(SMTP_CONFIGURATION_COLLECTION, "id", "configurationSetId", "smtpProperties"),
                Aggregation.project("id", "configurationSetName", "configurationSetDescriptoon", "emailTemplates", "smtpProperties")
        );
        return this.mongoTemplate.aggregate(aggregationPipelineConfig, CONFIGURATION_SET_COLLECTION, ConfigurationSetModel.class).getUniqueMappedResult();
    }

    @Override
    public ConfigurationSetModel getConfigurationSetByName(String configurationSetName, UUID tenantId) {
        Aggregation aggregationPipelineConfig = Aggregation.newAggregation(
          Aggregation.match(Criteria.where("tenantId").is(tenantId).and("configurationSetName").is(configurationSetName)),
                Aggregation.lookup(EMAILS_TEMPLATE_COLLECTION, "id", "configurationSetId", "emailTemplates"),
                Aggregation.lookup(SMTP_CONFIGURATION_COLLECTION, "id", "configurationSetId", "smtpProperties"),
                Aggregation.project("id", "configurationSetName", "configurationSetDescriptoon", "emailTemplates", "smtpProperties")
        );
        return this.mongoTemplate.aggregate(aggregationPipelineConfig, EMAILS_TEMPLATE_COLLECTION, ConfigurationSetModel.class).getUniqueMappedResult();
    }

    private List<EmailTemplateDocument> getEmailTemplates(UUID configurationSetId) {
        List<EmailTemplateAction> actions = List.of(EmailTemplateAction.values());
        List<EmailTemplateDocument> templates = new ArrayList<>();
        actions.forEach(action -> {
            EmailTemplateDocument emailTemplateDocument = EmailTemplateDocument.builder()
                    .configurationSetId(configurationSetId)
                    .metadata(MetadataUtils.getCreationAndUpdateTimestamps())
                    .templateName(action.name())
                    .build();
            templates.add(emailTemplateDocument);
        });
        return templates;
    }
}
