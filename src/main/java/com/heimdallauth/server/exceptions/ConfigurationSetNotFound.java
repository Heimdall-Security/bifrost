package com.heimdallauth.server.exceptions;

public class ConfigurationSetNotFound extends RuntimeException {
    public ConfigurationSetNotFound(String message) {
        super(message);
    }
}
