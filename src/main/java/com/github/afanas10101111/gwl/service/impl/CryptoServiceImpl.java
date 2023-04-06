package com.github.afanas10101111.gwl.service.impl;

import com.github.afanas10101111.gwl.exeption.HmacSignatureValidationException;
import com.github.afanas10101111.gwl.service.CryptoService;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class CryptoServiceImpl implements CryptoService {
    public static final String ALGORITHM = "HmacSHA256";
    public static final String SIGNATURE_PREFIX = "sha256=";
    public static final String ERROR = "invalid signature";

    @Value("${secret.token}")
    private String token;

    @Override
    public void validateHmacSignature(String payload, String signature) {
        try {
            Mac macInstance = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(token.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            macInstance.init(secretKeySpec);

            String computed = SIGNATURE_PREFIX + HexUtils.toHexString(
                    macInstance.doFinal(payload.getBytes(StandardCharsets.UTF_8))
            );
            if (!computed.equals(signature)) {
                throw new HmacSignatureValidationException(ERROR);
            }
        } catch (Exception ex) {
            throw new HmacSignatureValidationException(ex.getMessage());
        }
    }
}
