package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.ConfigurationSetAggregationModel;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ConfigurationMapper {

    ConfigurationSetModel toConfigurationSetModel(ConfigurationSetAggregationModel aggregationModel);
}
