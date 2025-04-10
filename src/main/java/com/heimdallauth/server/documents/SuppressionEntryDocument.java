package com.heimdallauth.server.documents;

import com.heimdallauth.server.constants.bifrost.SuppressionListEntryType;
import com.heimdallauth.server.constants.bifrost.SuppressionReason;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SuppressionEntryDocument {
    @Id
    private String id;
    private SuppressionListEntryType entryType;
    private String value;
    private SuppressionReason reason;
    private Instant createdAt;
    private Instant updatedAt;
}
