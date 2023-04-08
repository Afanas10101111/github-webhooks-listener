package com.github.afanas10101111.gwl.service;

public interface CryptoService {
    boolean hmacSignatureIsValid(String payload, String signature);
}
