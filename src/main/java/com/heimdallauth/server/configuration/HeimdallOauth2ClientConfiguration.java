package com.heimdallauth.server.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "heimdall.bifrost.security.oauth2.client")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HeimdallOauth2ClientConfiguration {
    private String clientId;
}
