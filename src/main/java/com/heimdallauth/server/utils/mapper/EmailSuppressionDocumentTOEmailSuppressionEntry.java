package com.heimdallauth.server.utils.mapper;

import com.heimdallauth.server.documents.SuppressionEntryDocument;
import com.heimdallauth.server.models.bifrost.SuppressionEntryModel;
import org.modelmapper.PropertyMap;

public class EmailSuppressionDocumentTOEmailSuppressionEntry extends PropertyMap<SuppressionEntryDocument, SuppressionEntryModel> {
    @Override
    protected void configure() {
        map().setSuppressionEntryId(source.getId());
        map().setValue(source.getValue());
        map().setReason(source.getReason());
        map().setSuppressionListEntryType(source.getEntryType());
    }
}
