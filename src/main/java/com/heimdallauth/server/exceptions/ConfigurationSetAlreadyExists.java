package com.heimdallauth.server.exceptions;

public class ConfigurationSetAlreadyExists extends RuntimeException {
    public ConfigurationSetAlreadyExists(String message) {
        super(message);
    }
}
