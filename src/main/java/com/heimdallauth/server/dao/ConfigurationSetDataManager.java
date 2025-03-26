package com.heimdallauth.server.dao;

import com.heimdallauth.server.constants.SmtpEncryption;
import com.heimdallauth.server.models.ConfigurationSetModel;
import com.heimdallauth.server.utils.HeimdallMetadata;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfigurationSetDataManager {
    boolean isConfigurationSetNameExist(String configurationSetName);
    void saveConfigurationSet(UUID tenantId, String configurationSetName, String configurationSetDescription);
    ConfigurationSetModel updateConfigurationSet(UUID configurationSetId, ConfigurationSetModel configurationSetModel);
    Optional<ConfigurationSetModel> getConfigurationSetById(UUID configurationSetId);
    Optional<ConfigurationSetModel> getConfigurationSetByName(String configurationSetName, UUID tenantId);
}
