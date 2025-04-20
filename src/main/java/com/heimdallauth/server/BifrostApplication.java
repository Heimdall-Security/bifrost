package com.heimdallauth.server;

import com.heimdallauth.server.configuration.HeimdallBifrostRoleConfiguration;
import com.heimdallauth.server.configuration.HeimdallOauth2ClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties({
        HeimdallBifrostRoleConfiguration.class,
        HeimdallOauth2ClientConfiguration.class
})
@EnableFeignClients
public class BifrostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BifrostApplication.class, args);
    }

}
