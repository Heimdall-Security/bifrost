package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.ConfigurationSetAggregationModel;
import com.heimdallauth.server.models.bifrost.ConfigurationSetModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {SmtpPropertiesMapper.class})
public interface ConfigurationMapper {

    @Mapping(source = "configurationId", target = "configurationSetId")
    @Mapping(source = "aggregationModel.smtpProperties", target = "smtpProperties", qualifiedByName = "toSmtpProperties")
    ConfigurationSetModel toConfigurationSetModel(ConfigurationSetAggregationModel aggregationModel);
}
