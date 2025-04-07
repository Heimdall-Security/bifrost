package com.heimdallauth.server.services;


import com.heimdallauth.server.dto.bifrost.CreateSmtpPropertiesDTO;
import com.heimdallauth.server.exceptions.ConfigurationSetNotFound;
import com.heimdallauth.server.exceptions.SmtpPropertiesExist;
import com.heimdallauth.server.models.bifrost.SmtpProperties;

import java.util.UUID;

public interface SmtpPropertiesManagementService {
    void createSmtpProperties(UUID configurationSetId, CreateSmtpPropertiesDTO smtpProperties) throws ConfigurationSetNotFound, SmtpPropertiesExist;

    void updateSmtpProperties(UUID configurationSetId, SmtpProperties smtpProperties) throws ConfigurationSetNotFound;

    void deleteSmtpProperties(UUID configurationSetId) throws ConfigurationSetNotFound;
}
