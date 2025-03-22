package com.heimdallauth.server.models;

import com.heimdallauth.server.constants.SmtpEncryption;
import lombok.*;
import org.checkerframework.checker.units.qual.N;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SmtpProperties {
    private String smtpHost;
    private String smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpProtocol;
    private SmtpEncryption encryption;
}
