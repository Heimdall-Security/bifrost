package com.heimdallauth.server;

import com.heimdallauth.server.configuration.HeimdallBifrostRoleConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        HeimdallBifrostRoleConfiguration.class
})
public class BifrostApplication {

    public static void main(String[] args) {
        SpringApplication.run(BifrostApplication.class, args);
    }

}
