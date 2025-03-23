package com.heimdallauth.server.dao.documents;

import com.heimdallauth.server.bifrost.SuppressionListEntry;
import com.heimdallauth.server.utils.HeimdallMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.heimdallauth.server.constants.MongoCollectionNames.SUPPRESSION_LIST_COLLECTION;

@Document(collection = SUPPRESSION_LIST_COLLECTION)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SuppressionListDocument {
    @Id
    private UUID id;
    @Indexed
    private UUID configurationSetId;
    private List<HeimdallMetadata> metadata;
    private List<SuppressionListEntry> suppressions;
    private Instant createdOn;
    private Instant updatedOn;
}
