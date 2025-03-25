package com.heimdallauth.server.services;

import com.heimdallauth.server.dao.ConfigurationSetDataManager;
import com.heimdallauth.server.exceptions.ConfigurationSetAlreadyExists;
import com.heimdallauth.server.exceptions.HeimdallBifrostBadDataException;
import com.heimdallauth.server.models.ConfigurationSetModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static com.heimdallauth.server.constants.HeimdallBifrostExceptionMessages.CONFIGURATION_SET_EXISTS;
import static com.heimdallauth.server.constants.HeimdallBifrostExceptionMessages.INVALID_REQUEST_CONFIG;

@Service
@Slf4j
public class ConfigurationSetManagementService {
    private final ConfigurationSetDataManager configurationSetDataManager;

    @Autowired
    public ConfigurationSetManagementService(ConfigurationSetDataManager configurationSetDataManager) {
        this.configurationSetDataManager = configurationSetDataManager;
    }

    public ConfigurationSetModel getConfigurationSetModel(String configurationSetId){
        try{
            UUID configurationSetUUID = UUID.fromString(configurationSetId);
            return configurationSetDataManager.getConfigurationSetById(configurationSetUUID);
        }catch (IllegalArgumentException e){
            log.error("Invalid configuration set id: {}", configurationSetId);
            return null;
        }
    }

    public ConfigurationSetModel getConfigurationSet(Optional<UUID> configurationSetId, Optional<String> configurationSetName, Optional<UUID> tenantID) {
        if (configurationSetId.isPresent()) {
            return this.configurationSetDataManager.getConfigurationSetById(configurationSetId.get());
        } else if (configurationSetName.isPresent() && tenantID.isPresent()) {
            return this.configurationSetDataManager.getConfigurationSetByName(configurationSetName.get(), tenantID.get());
        }
        throw new HeimdallBifrostBadDataException(INVALID_REQUEST_CONFIG);
    }

    public ConfigurationSetModel updateConfigurationSet(ConfigurationSetModel configurationSetModel) {
        
    }
    public ConfigurationSetModel createConfigurationSet(String tenantId, String configurationSetName, String configurationSetDescription) throws HeimdallBifrostBadDataException {
        try{
            validateConfigurationSetName(configurationSetName);
            this.configurationSetDataManager.saveConfigurationSet(UUID.fromString(tenantId), configurationSetName, configurationSetDescription);
            return this.configurationSetDataManager.getConfigurationSetByName(configurationSetName, UUID.fromString(tenantId) );
        }catch (ConfigurationSetAlreadyExists e){
            log.error("Configuration set with the same name already exists: {}", configurationSetName);
            throw new HeimdallBifrostBadDataException(e.getMessage());
        }catch (IllegalArgumentException e){
            log.error("Invalid tenant id: {}", tenantId);
            throw new HeimdallBifrostBadDataException("Invalid tenant id");
        }
    }

    private void validateConfigurationSetName(String configurationSetName){
        if(this.configurationSetDataManager.isConfigurationSetNameExist(configurationSetName)){
            throw new ConfigurationSetAlreadyExists(CONFIGURATION_SET_EXISTS);
        }
    }
}
