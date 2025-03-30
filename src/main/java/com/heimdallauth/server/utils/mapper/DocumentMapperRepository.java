package com.heimdallauth.server.utils.mapper;

import org.modelmapper.ModelMapper;

public class DocumentMapperRepository {
    public static ModelMapper DOCUMENT_MAPPER = new ModelMapper();

    static {
        DOCUMENT_MAPPER.addMappings(new EmailSuppressionDocumentTOEmailSuppressionEntry());
        DOCUMENT_MAPPER.addMappings(new ConfigurationSetAggregationModelToConfigurationSetModel());
    }
}
