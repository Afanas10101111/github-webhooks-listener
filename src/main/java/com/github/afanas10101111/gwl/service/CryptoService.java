package com.github.afanas10101111.gwl.service;

public interface CryptoService {
    void validateHmacSignature(String payload, String signature);
}
