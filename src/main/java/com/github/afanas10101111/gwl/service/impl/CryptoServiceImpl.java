package com.github.afanas10101111.gwl.service.impl;

import com.github.afanas10101111.gwl.service.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
public class CryptoServiceImpl implements CryptoService {
    public static final String ALGORITHM = "HmacSHA256";

    @Value("${secret.token}")
    private String token;

    @Override
    public boolean hmacSignatureIsValid(String payload, String signature) {
        try {
            Mac macInstance = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            macInstance.init(secretKeySpec);

            String computed = HexUtils.toHexString(macInstance.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return computed.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NullPointerException ex) {
            log.error("hmacSignatureIsValid -> {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
            return false;
        }
    }
}
