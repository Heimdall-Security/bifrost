package com.heimdallauth.server.documents;

import com.heimdallauth.server.models.bifrost.SmtpProperties;
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
    private SmtpProperties smtpProperties;
}
