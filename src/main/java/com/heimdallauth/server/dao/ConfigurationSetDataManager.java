package com.heimdallauth.server.dao;

import com.heimdallauth.server.constants.SmtpEncryption;
import com.heimdallauth.server.models.ConfigurationSetModel;
import com.heimdallauth.server.utils.HeimdallMetadata;

import java.util.List;
import java.util.UUID;

public interface ConfigurationSetDataManager {
    boolean isConfigurationSetNameExist(String configurationSetName);
    void saveConfigurationSet(String configurationSetName, String configurationSetDescription);
    void updateConfigurationSetTemplate(String configurationSetId, String templateName, List<HeimdallMetadata> metadata, String subject, String richBodyContent, String textBodyContent);
    void updateConfigurationSetSuppressionList(String configurationSetId, String suppressionListEntry, String suppressionListEntryType);
    void updateConfigurationSetSmtpProperties(String configurationSetId, String host, String port, String username, String password, SmtpEncryption encryption);
    void deleteConfigurationSet(String configurationSetId);
    ConfigurationSetModel getConfigurationSetById(UUID configurationSetId);
    ConfigurationSetModel getConfigurationSetByName(String configurationSetName, UUID tenantId);
}
