package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.dto.bifrost.CreateConfigurationSetDTO;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.services.ConfigurationSetManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/management/configuration-set")
@Tag(name = "Configuration Set Management Controller", description = "Controller for managing configuration sets")
public class ConfigurationSetManagementController {
    private final ConfigurationSetManagementService configurationSetManagementService;

    public ConfigurationSetManagementController(ConfigurationSetManagementService configurationSetManagementService) {
        this.configurationSetManagementService = configurationSetManagementService;
    }
    @GetMapping("/{configurationSetId}")
    public ResponseEntity<ConfigurationSetModel> getConfigurationSet(@PathVariable UUID configurationSetId) {
        return ResponseEntity.ok(this.configurationSetManagementService.getConfigurationSetById(configurationSetId));
    }
    @PostMapping("/create")
    public ResponseEntity<ConfigurationSetModel> createNewConfigurationSet(@RequestBody CreateConfigurationSetDTO createConfigurationSetDTO, @RequestParam("force") boolean force){
        ConfigurationSetModel createdConfigurationSet = this.configurationSetManagementService.createNewConfigurationSet(createConfigurationSetDTO, UUID.randomUUID(), force);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdConfigurationSet.configurationSetId()).toUri()).build();
    }
    @GetMapping
    public ResponseEntity<List<ConfigurationSetModel>> getConfigurationSetForTenantId(@RequestParam("tenantId") UUID tenantId){
        return ResponseEntity.ok(this.configurationSetManagementService.getConfigurationSetsForTenantId(tenantId));
    }
}
