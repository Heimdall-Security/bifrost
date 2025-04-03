package com.heimdallauth.server.controllers.v1.management;

import com.heimdallauth.server.configuration.HeimdallBifrostRoleConfiguration;
import com.heimdallauth.server.dto.bifrost.CreateSuppressionEntryDTO;
import com.heimdallauth.server.exceptions.SuppressionListNotFound;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import com.heimdallauth.server.services.EmailSuppressionManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/management/suppression-entry")
@Tag(name = "Suppression Entry Management Controller", description = "Controller for managing suppression entries")
public class SuppressionEntryManagementController {
    private final EmailSuppressionManagementService emailSuppressionManagementService;

    @Autowired
    public SuppressionEntryManagementController(EmailSuppressionManagementService emailSuppressionManagementService) {
        this.emailSuppressionManagementService = emailSuppressionManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_READ) or hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    public ResponseEntity<List<SuppressionEntryModel>> getAllSuppressionEntries(){
        return ResponseEntity.ok(emailSuppressionManagementService.getAllSuppressionEntries());
    }
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_READ) or hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    @GetMapping("/{suppressionEntryId}")
    public ResponseEntity<SuppressionEntryModel> getSuppressionEntryById(@PathVariable UUID suppressionEntryId) throws SuppressionListNotFound {
        return ResponseEntity.ok(this.emailSuppressionManagementService.getSuppressionEntryById(suppressionEntryId));
    }
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    @PostMapping
    public ResponseEntity<SuppressionEntryModel> createNewSuppressionEntry(@RequestBody CreateSuppressionEntryDTO createSuppressionEntryDTO) throws SuppressionListNotFound {
        SuppressionEntryModel createdSuppressionEntry = this.emailSuppressionManagementService.createSuppressionEntry(createSuppressionEntryDTO);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdSuppressionEntry.getSuppressionEntryId()).toUri()).build();
    }
    @PreAuthorize("hasRole(@heimdallBifrostRoleConfiguration.ROLE_MANAGEMENT_WRITE)")
    @DeleteMapping("/{suppressionEntryId}")
    public ResponseEntity<Void> deleteSuppressionEntryById(@PathVariable UUID suppressionEntryId) throws SuppressionListNotFound {
        this.emailSuppressionManagementService.deleteSuppressionEntryById(suppressionEntryId);
        return ResponseEntity.ok().build();
    }
}
