package com.heimdallauth.server.dao.documents;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

import static com.heimdallauth.server.constants.MongoCollectionNames.CONFIGURATION_SET_COLLECTION;

@Document(collection = CONFIGURATION_SET_COLLECTION)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfigurationSetMaster {
    @Id
    private UUID id;
    private String configurationSetName;
    private String configurationSetDescriptoon;
    @Indexed(unique = false)
    private UUID tenantId;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean active;
}
