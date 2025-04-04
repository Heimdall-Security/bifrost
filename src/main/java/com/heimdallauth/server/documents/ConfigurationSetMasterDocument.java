package com.heimdallauth.server.documents;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
