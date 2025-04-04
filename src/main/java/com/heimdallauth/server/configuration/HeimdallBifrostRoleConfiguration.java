package com.heimdallauth.server.configuration;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "heimdall.bifrost.security.oauth2.roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Component
public class HeimdallBifrostRoleConfiguration {
    private String ROLE_MANAGEMENT_READ;
    private String ROLE_MANAGEMENT_WRITE;
}
