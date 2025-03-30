package com.heimdallauth.server.documents;

import com.heimdallauth.server.constants.bifrost.SuppressionListEntryType;
import com.heimdallauth.server.constants.bifrost.SuppressionReason;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SuppressionEntryDocument {
    @Id
    private UUID id;
    private SuppressionListEntryType entryType;
    private String value;
    private SuppressionReason reason;
    private Instant createdAt;
    private Instant updatedAt;
}
