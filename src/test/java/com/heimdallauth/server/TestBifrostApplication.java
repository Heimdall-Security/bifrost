package com.heimdallauth.server;

import org.springframework.boot.SpringApplication;

public class TestBifrostApplication {

    public static void main(String[] args) {
        SpringApplication.from(BifrostApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
