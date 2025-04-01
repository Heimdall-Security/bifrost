package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.SuppressionEntryDocument;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SuppressionEntryMapper {

    @Mapping(source = "id", target = "suppressionEntryId")
    @Mapping(source="entryType", target = "suppressionListEntryType")
    SuppressionEntryModel map(SuppressionEntryDocument suppressionEntryDocument);

    @InheritInverseConfiguration
    SuppressionEntryDocument map(SuppressionEntryModel suppressionEntryModel);
}
