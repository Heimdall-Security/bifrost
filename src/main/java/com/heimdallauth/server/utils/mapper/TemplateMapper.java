package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.TemplateDocument;
import com.heimdallauth.server.models.bifrost.Template;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TemplateMapper {

    @Mapping(source = "id", target = "templateId")
    @Mapping(source="defaultMessageHeaders", target="defaultEmailHeaders")
    Template mapToTemplateModel(TemplateDocument document);
}
