package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.ConfigurationSetAggregationModel;
import com.heimdallauth.server.documents.SuppressionEntryDocument;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class ConfigurationSetAggregationModelToConfigurationSetModel extends PropertyMap<ConfigurationSetAggregationModel, ConfigurationSetModel> {
    private static final ModelMapper MODEL_MAPPER = new ModelMapper();
    static {
        MODEL_MAPPER.addMappings(new EmailSuppressionDocumentTOEmailSuppressionEntry());
    }
    @Override
    protected void configure() {
        map().setSuppressionEntries(source.getSuppressionEntries().stream().map(ConfigurationSetAggregationModelToConfigurationSetModel::toSuppressionEntryModel).toList());
        map().setConfigurationSetId(source.getConfigurationId());
        map().setConfigurationSetDescription(source.getConfigurationSetDescription());
        map().setEnabled(true);
        map().setSmtpProperties(null);
    }
    private static SuppressionEntryModel toSuppressionEntryModel(SuppressionEntryDocument entryDocument){
        return MODEL_MAPPER.map(entryDocument, SuppressionEntryModel.class);
    }
}
