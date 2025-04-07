package com.heimdallauth.server.documents;

import com.heimdallauth.server.constants.bifrost.EmailConnectionType;
import com.heimdallauth.server.constants.bifrost.SmtpAuthenticationMethod;
import com.heimdallauth.server.constants.bifrost.SmtpConnectionSecurity;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SmtpPropertiesDocument {
    @Id
    private String id;
    private String host;
    private int port;
    private int connectionLimit;
    private String smtpServerLoginUsername;
    private String smtpServerLoginPassword;
    private SmtpAuthenticationMethod authenticationMethod;
    private String fromEmailAddress;
    private EmailConnectionType emailConnectionType;
    private SmtpConnectionSecurity smtpConnectionSecurity;
}
