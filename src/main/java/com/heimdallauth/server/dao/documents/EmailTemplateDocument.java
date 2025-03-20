package com.heimdallauth.server.dao.documents;

import com.heimdallauth.server.utils.HeimdallMetadata;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "email_template")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmailTemplateDocument {
    @Id
    private UUID id;
    private UUID configurationSetId;
    private String templateName;
    private List<HeimdallMetadata> metadata;
    private String subject;
    private String richBodyContent;
    private String textBodyContent;
}
