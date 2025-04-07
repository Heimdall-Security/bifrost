package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.CreateSmtpPropertiesDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.models.bifrost.SmtpProperties;
import com.heimdallauth.server.services.SmtpPropertiesManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/smtp")
@Tag(name = "ManagementController", description = "Controller for Managing Configuration for Service")
public class SmtpPropertiesManagementController {
    private final SmtpPropertiesManagementService smtpPropertiesManagementService;

    public SmtpPropertiesManagementController(SmtpPropertiesManagementService smtpPropertiesManagementService) {
        this.smtpPropertiesManagementService = smtpPropertiesManagementService;
    }
    @PostMapping("/create")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_SMTP_WRITE)")
    public ResponseEntity<Void> createNewSmtpProperties(@RequestParam("configurationSetId") UUID configurationSetId, @RequestBody CreateSmtpPropertiesDTO createSmtpPropertiesDTO) throws ConfigurationSetNotFound {
        this.smtpPropertiesManagementService.createSmtpProperties(configurationSetId, createSmtpPropertiesDTO);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/update/{configurationSetId}")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_SMTP_WRITE)")
    public ResponseEntity<Void> updateSmtpProperties(@PathVariable("configurationSetId") UUID configurationSetId, @RequestBody SmtpProperties smtpProperties) throws ConfigurationSetNotFound {
        this.smtpPropertiesManagementService.updateSmtpProperties(configurationSetId, smtpProperties);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_SMTP_WRITE)")
    public ResponseEntity<Void> deleteSmtpProperties(@RequestParam("configurationSetId") UUID configurationSetId) throws ConfigurationSetNotFound {
        this.smtpPropertiesManagementService.deleteSmtpProperties(configurationSetId);
        return ResponseEntity.ok().build();
    }
}
