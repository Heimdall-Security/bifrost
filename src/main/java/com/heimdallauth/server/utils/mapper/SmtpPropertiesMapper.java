package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.SmtpPropertiesDocument;
import com.heimdallauth.server.models.bifrost.SmtpProperties;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SmtpPropertiesMapper {

    @Mapping(source = "host", target = "serverAddress")
    @Mapping(source = "port", target = "portNumber")
    @Mapping(source = "smtpServerLoginUsername", target = "loginUsername")
    @Mapping(source = "smtpServerLoginPassword", target = "loginPassword")
    @Mapping(source = "fromEmailAddress", target = "fromEmailAddress")
    @Named("toSmtpProperties")
    SmtpProperties toSmtpProperties(SmtpPropertiesDocument smtpPropertiesDocument);
}
