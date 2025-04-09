package com.heimdallauth.server.exceptions;

public class TemplateAlreadyExists extends RuntimeException {
    public TemplateAlreadyExists(String message) {
        super(message);
    }
}
