package com.heimdallauth.server.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationSetAggregationModel extends ConfigurationSetMasterDocument {
    private List<SuppressionEntryDocument> suppressionEntries;
}
