package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.SuppressionEntryDocument;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SuppressionEntryMapper {

    @Mapping(source = "id", target = "suppressionEntryId")
    @Mapping(source = "entryType", target = "suppressionListEntryType")
    @Named("toSuppressionEntryModel")
    SuppressionEntryModel map(SuppressionEntryDocument suppressionEntryDocument);

    @InheritInverseConfiguration
    SuppressionEntryDocument map(SuppressionEntryModel suppressionEntryModel);
}
