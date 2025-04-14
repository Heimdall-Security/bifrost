package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.CreateEmailTemplateDTO;
import com.heimdallauth.server.models.bifrost.Template;
import com.heimdallauth.server.services.TemplateManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/templates")
@Tag(name = "Template Management", description = "Management of email templates")
public class TemplateManagementController {
    private final TemplateManagementService templateManagementService;

    public TemplateManagementController(TemplateManagementService templateManagementService) {
        this.templateManagementService = templateManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_EMAIL_TEMPLATE_READ_TENANT)")
    public ResponseEntity<List<Template>> getAllTemplatesByTenantId(@RequestParam("tenantId") UUID tenantId) {
        return ResponseEntity.ok(templateManagementService.getAllTemplatesByTenantId(tenantId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_EMAIL_TEMPLATE_WRITE_TENANT)")
    public ResponseEntity<List<Template>> getAllHostedTemplates() {
        return ResponseEntity.ok(templateManagementService.getAllTemplates());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_EMAIL_TEMPLATE_WRITE_TENANT)")
    public ResponseEntity<Template> createTemplate(@RequestBody CreateEmailTemplateDTO createEmailTemplateDTO) {
        Template createdTemplate = templateManagementService.createNewTemplate(createEmailTemplateDTO);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{templateId}")
                .buildAndExpand(createdTemplate.templateName())
                .toUri()).body(createdTemplate);
    }

    @GetMapping("/{templateId}")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_EMAIL_TEMPLATE_READ)")
    public ResponseEntity<Template> getTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(templateManagementService.getTemplateById(templateId));
    }
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_EMAIL_TEMPLATE_WRITE_TENANT)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID templateId) {
        this.templateManagementService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }
}
