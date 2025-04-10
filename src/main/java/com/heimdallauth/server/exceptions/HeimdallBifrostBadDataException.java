package com.heimdallauth.server.exceptions;

public class HeimdallBifrostBadDataException extends RuntimeException {
    public HeimdallBifrostBadDataException(String message) {
        super(message);
    }

    public HeimdallBifrostBadDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
