package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.CreateSuppressionEntryDTO;
import com.heimdallauth.server.exceptions.SuppressionListNotFound;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;

import java.util.List;
import java.util.UUID;

public interface EmailSuppressionManagementService {
    List<SuppressionEntryModel> getAllSuppressionEntriesById(List<UUID> suppressionEntryId) throws SuppressionListNotFound;
    SuppressionEntryModel createSuppressionEntry(CreateSuppressionEntryDTO createSuppressionEntryPayload);
    SuppressionEntryModel getSuppressionEntryByConfigurationSetId(String configurationSetId) throws SuppressionListNotFound;
    void deleteSuppressionEntryById(String suppressionEntryId);
}
