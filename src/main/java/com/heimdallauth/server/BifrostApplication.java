package com.heimdallauth.server;

import com.heimdallauth.server.configuration.HeimdallBifrostRoleConfiguration;
import com.heimdallauth.server.configuration.HeimdallOauth2ClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        HeimdallBifrostRoleConfiguration.class,
        HeimdallOauth2ClientConfiguration.class
})
public class BifrostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BifrostApplication.class, args);
    }

}
