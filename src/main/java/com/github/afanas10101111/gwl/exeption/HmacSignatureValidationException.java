package com.github.afanas10101111.gwl.exeption;

public class HmacSignatureValidationException extends RuntimeException {
    public HmacSignatureValidationException(String message) {
        super(message);
    }
}
