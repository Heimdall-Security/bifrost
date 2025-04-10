package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.CreateSuppressionEntryDTO;
import com.heimdallauth.server.exceptions.SuppressionListNotFound;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;

import java.util.List;
import java.util.UUID;

public interface EmailSuppressionManagementService {
    List<SuppressionEntryModel> getAllSuppressionEntries();

    List<SuppressionEntryModel> getAllSuppressionEntriesById(List<UUID> suppressionEntryId) throws SuppressionListNotFound;

    SuppressionEntryModel createSuppressionEntry(CreateSuppressionEntryDTO createSuppressionEntryPayload);

    SuppressionEntryModel getSuppressionEntryById(UUID suppressionEntryId) throws SuppressionListNotFound;

    List<SuppressionEntryModel> getSuppressionEntryByConfigurationSetId(UUID configurationSetId) throws SuppressionListNotFound;

    void deleteSuppressionEntryById(UUID suppressionEntryId);
}
