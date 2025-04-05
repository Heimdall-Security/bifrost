package com.heimdallauth.server.documents;

import com.heimdallauth.server.constants.EmailTemplateAction;
import com.heimdallauth.server.models.bifrost.EmailTemplateContent;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TemplateDocument {
    @Id
    private String id;
    private String templateName;
    private EmailTemplateAction templateAction;
    private String templateId;
    private EmailTemplateContent content;

}
