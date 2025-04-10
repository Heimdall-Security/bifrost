package com.heimdallauth.server.exceptions;

public class TemplateNotFound extends RuntimeException {
    public TemplateNotFound(String message) {
        super(message);
    }
}
