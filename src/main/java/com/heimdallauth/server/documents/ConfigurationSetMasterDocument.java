package com.heimdallauth.server.documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationSetMasterDocument {
    @Id
    private String configurationId;
    private String configurationSetName;
    private boolean isEnabled;
    private String configurationSetDescription;
    @Indexed
    private String tenantId;
    @Indexed
    private List<String> suppressionListIds;
    private Instant createdAt;
    private Instant updatedAt;
}
