package com.github.afanas10101111.gwl.service;

import com.github.afanas10101111.gwl.exeption.HmacSignatureValidationException;
import com.github.afanas10101111.gwl.service.impl.CryptoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = CryptoServiceImpl.class)
class CryptoServiceTest {
    public static final String PAYLOAD = "some data";
    public static final String VALID_SIGNATURE
            = CryptoServiceImpl.SIGNATURE_PREFIX + "9a8acf0183fb80e5396d2a4183d040dc2f151be08da1705df7d41e68d2f354ea";
    public static final String INVALID_SIGNATURE
            = CryptoServiceImpl.SIGNATURE_PREFIX + "0a8acf0183fb80e5396d2a4183d040dc2f151be08da1705df7d41e68d2f354ea";

    @Autowired
    private CryptoService cryptoService;

    @Test
    void validationOfValidHmacSignatureShouldNotThrowException() {
        assertDoesNotThrow(() -> cryptoService.validateHmacSignature(PAYLOAD, VALID_SIGNATURE));
    }

    @Test
    void validationOfInvalidHmacSignatureShouldThrowMacValidationException() {
        HmacSignatureValidationException exception = assertThrows(
                HmacSignatureValidationException.class,
                () -> cryptoService.validateHmacSignature(PAYLOAD, INVALID_SIGNATURE)
        );
        assertThat(exception.getMessage()).isEqualTo(CryptoServiceImpl.ERROR);
    }

    @Test
    void nullPayloadShouldThrowMacValidationException() {
        HmacSignatureValidationException exception = assertThrows(
                HmacSignatureValidationException.class,
                () -> cryptoService.validateHmacSignature(null, INVALID_SIGNATURE)
        );
        assertThat(exception.getMessage()).isNotEqualTo(CryptoServiceImpl.ERROR);
    }
}
