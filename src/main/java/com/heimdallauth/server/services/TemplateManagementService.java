package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.CreateEmailTemplateDTO;
import com.heimdallauth.server.models.bifrost.Template;

import java.util.List;
import java.util.UUID;

public interface TemplateManagementService {
    Template getTemplateById(UUID templateId);
    List<Template> getTemplateByTenantIdAndTemplateName(UUID tenantId, String templateName);
    Template createNewTemplate(CreateEmailTemplateDTO createEmailTemplateDTO);
    List<Template> updateTemplate(UUID templateId, CreateEmailTemplateDTO createEmailTemplateDTO);
    void deleteTemplate(UUID templateId);
}
