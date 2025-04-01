package com.heimdallauth.server.services;

import com.heimdallauth.server.dto.bifrost.ConfigurationSetDTO;
import com.heimdallauth.server.dto.bifrost.CreateConfigurationSetDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetAlreadyExists;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;

import java.util.UUID;

public interface ConfigurationSetManagementService {
    ConfigurationSetModel createNewConfigurationSet(CreateConfigurationSetDTO createConfigurationSetPayload, UUID tenantID, boolean force) throws ConfigurationSetAlreadyExists;
    ConfigurationSetModel getConfigurationSetById(UUID configurationSetId) throws ConfigurationSetNotFound;
    ConfigurationSetModel getConfigurationSetByNameAndTenantId(String configurationSetName, String tenantId) throws ConfigurationSetNotFound;
    ConfigurationSetModel updateConfigurationSetMasterData(String configurationSetId, String configurationSetName, String configurationSetDescription) throws ConfigurationSetNotFound;
}
