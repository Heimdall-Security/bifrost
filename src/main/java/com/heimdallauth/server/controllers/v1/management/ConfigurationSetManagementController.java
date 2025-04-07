package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.CreateConfigurationSetDTO;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.services.ConfigurationSetManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/configuration-set")
@Tag(name = "Configuration Set Management Controller", description = "Controller for managing configuration sets")
public class ConfigurationSetManagementController {
    private final ConfigurationSetManagementService configurationSetManagementService;

    public ConfigurationSetManagementController(ConfigurationSetManagementService configurationSetManagementService) {
        this.configurationSetManagementService = configurationSetManagementService;
    }
    @GetMapping("/{configurationSetId}")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_CONFIGURATION_SET_READ)")
    public ResponseEntity<ConfigurationSetModel> getConfigurationSet(@PathVariable UUID configurationSetId) {
        return ResponseEntity.ok(this.configurationSetManagementService.getConfigurationSetById(configurationSetId));
    }
    @PostMapping("/create")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_CONFIGURATION_SET_WRITE)")
    public ResponseEntity<ConfigurationSetModel> createNewConfigurationSet(@RequestBody CreateConfigurationSetDTO createConfigurationSetDTO, @RequestParam("force") boolean force){
        ConfigurationSetModel createdConfigurationSet = this.configurationSetManagementService.createNewConfigurationSet(createConfigurationSetDTO, createConfigurationSetDTO.tenantId(), force);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdConfigurationSet.configurationSetId()).toUri()).build();
    }
    @GetMapping
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_CONFIGURATION_SET_WRITE)")
    public ResponseEntity<List<ConfigurationSetModel>> getConfigurationSetForTenantId(@RequestParam("tenantId") UUID tenantId){
        return ResponseEntity.ok(this.configurationSetManagementService.getConfigurationSetsForTenantId(tenantId));
    }
    @DeleteMapping("/{configurationSetId}")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    public ResponseEntity<Void> deleteConfigurationSetById(@PathVariable("configurationSetId") UUID configurationSetId){
        this.configurationSetManagementService.deleteConfigurationSetById(configurationSetId);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{configurationSetId}/status")
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    public ResponseEntity<ConfigurationSetModel> updateConfigurationSetStatus(@PathVariable("configurationSetId") UUID configurationSetId, @RequestParam("isEnabled") boolean isEnabled){
        return ResponseEntity.ok(this.configurationSetManagementService.updateConfigurationSetStatus(configurationSetId, isEnabled));
    }
}
