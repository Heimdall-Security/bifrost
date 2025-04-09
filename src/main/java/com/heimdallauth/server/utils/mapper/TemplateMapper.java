package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.TemplateDocument;
import com.heimdallauth.server.models.bifrost.Template;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TemplateMapper {

    Template mapToTemplateModel(TemplateDocument document);
}
