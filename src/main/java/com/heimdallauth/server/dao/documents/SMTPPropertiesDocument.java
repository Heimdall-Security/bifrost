package com.heimdallauth.server.dao.documents;

import com.heimdallauth.server.constants.SmtpEncryption;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SMTPPropertiesDocument {
    @Id
    private UUID id;
    @Indexed
    private UUID configurationSetId;
    private String host;
    private String port;
    private String username;
    private String password;
    private SmtpEncryption encryption;
}
