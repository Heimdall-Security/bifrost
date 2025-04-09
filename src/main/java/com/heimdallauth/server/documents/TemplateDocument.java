package com.heimdallauth.server.documents;

import com.heimdallauth.server.constants.EmailTemplateAction;
import com.heimdallauth.server.models.bifrost.EmailContent;
import com.heimdallauth.server.models.bifrost.MessageHeader;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TemplateDocument {
    @Id
    private String id;
    @Indexed(unique = false)
    private String tenantId;
    private String templateName;
    private EmailContent content;
    private List<MessageHeader> defaultMessageHeaders;
    private Instant createdAt;
    private Instant updatedAt;
}
