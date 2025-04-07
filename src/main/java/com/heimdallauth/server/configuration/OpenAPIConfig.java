package com.heimdallauth.server.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                clientCredentials = @OAuthFlow(
                        tokenUrl = "https://keycloak-prod.ap-west-1.heimdallauth.com/realms/mayanksoni-tech-dev",
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenID scope")
                        }
                )
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "Authorization"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Heimdall Bifrost API",
                version = "1.0",
                description = "API documentation for Heimdall Bifrost"
        ),
        security = {
                @SecurityRequirement(
                        name = "oauth2",
                        scopes = {"openid", "profile", "email"}
                ),
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
public class OpenAPIConfig {
}
