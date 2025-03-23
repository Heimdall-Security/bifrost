package com.heimdallauth.server.services;

import com.heimdallauth.server.dao.ConfigurationSetDataManager;
import com.heimdallauth.server.exceptions.HeimdallBifrostBadDataException;
import com.heimdallauth.server.models.ConfigurationSetModel;
import com.heimdallauth.server.models.EmailTemplateModel;
import com.heimdallauth.server.models.SmtpProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ConfigurationSetManagementServiceTest {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationSetManagementServiceTest.class);
    private ConfigurationSetManagementService configurationSetManagementService;

    private static final String TEST_CONFIGURATION_SET_ID = UUID.randomUUID().toString();
    private static final String TEST_CONFIGURATION_SET_NAME = "Test Configuration Set";
    private static final String TEST_CONFIGURATION_SET_DESCRIPTION = "Test Configuration Set Description";
    private static final List<EmailTemplateModel> TEST_TEMPLATES = List.of();
    private static final ConfigurationSetModel TEST_CONFIGURATION_SET_MODEL = ConfigurationSetModel.builder()
            .configurationSetId(TEST_CONFIGURATION_SET_ID)
            .configurationSetName(TEST_CONFIGURATION_SET_NAME)
            .configurationSetDescription(TEST_CONFIGURATION_SET_DESCRIPTION)
            .templates(TEST_TEMPLATES)
            .smtpProperties(SmtpProperties.builder().build())
            .build();
    @Mock
    private ConfigurationSetDataManager configurationSetDataManager;

    @BeforeEach
    public void setup(){
        try{
            MockitoAnnotations.openMocks(this);
            this.configurationSetManagementService = new ConfigurationSetManagementService(configurationSetDataManager);
        }catch (Exception e){
            log.debug(e.getMessage());
        }
    }
    @Test
    public void testCreateConfigurationSet_withUniqueConfigurationSetName(){
        when(configurationSetDataManager.isConfigurationSetNameExist(Mockito.anyString())).thenReturn(Boolean.FALSE);
        when(configurationSetDataManager.getConfigurationSetByName(Mockito.anyString(), Mockito.any())).thenReturn(TEST_CONFIGURATION_SET_MODEL);
        configurationSetManagementService.createConfigurationSet(UUID.randomUUID().toString(), TEST_CONFIGURATION_SET_NAME, TEST_CONFIGURATION_SET_DESCRIPTION);
    }
    @Test
    public void testCreateConfigurationSet_withNonUniqueConfigurationSetName(){
        when(configurationSetDataManager.isConfigurationSetNameExist(Mockito.anyString())).thenReturn(Boolean.TRUE);
        assertThrows(HeimdallBifrostBadDataException.class, () -> configurationSetManagementService.createConfigurationSet(TEST_CONFIGURATION_SET_ID, TEST_CONFIGURATION_SET_NAME, TEST_CONFIGURATION_SET_DESCRIPTION));
    }
}