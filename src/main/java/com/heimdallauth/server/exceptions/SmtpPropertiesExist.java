package com.heimdallauth.server.exceptions;

public class SmtpPropertiesExist extends RuntimeException {
    public SmtpPropertiesExist(String message) {
        super(message);
    }
}
