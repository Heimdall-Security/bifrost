package com.heimdallauth.server.documents;

import com.heimdallauth.server.utils.HeimdallMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document
public class AuditTrailDocument {
    @Id
    private UUID id;
    private UUID transactionId;
    private String auditClassName;
    private List<HeimdallMetadata> metadata;
    private Class<?> oldValue;
    private Class<?> newValue;
    private Instant updateTimestamp;
}
