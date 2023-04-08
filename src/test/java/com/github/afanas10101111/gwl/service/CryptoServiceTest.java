package com.github.afanas10101111.gwl.service;

import com.github.afanas10101111.gwl.service.impl.CryptoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CryptoServiceImpl.class)
class CryptoServiceTest {
    public static final String PAYLOAD = "some data";
    public static final String VALID_SIGNATURE = "9a8acf0183fb80e5396d2a4183d040dc2f151be08da1705df7d41e68d2f354ea";
    public static final String INVALID_SIGNATURE = "0a8acf0183fb80e5396d2a4183d040dc2f151be08da1705df7d41e68d2f354ea";

    @Autowired
    private CryptoService cryptoService;

    @Test
    void validationResultOfValidHmacSignatureShouldBeTrue() {
        assertThat(cryptoService.hmacSignatureIsValid(PAYLOAD, VALID_SIGNATURE)).isTrue();
    }

    @Test
    void validationResultOfInvalidHmacSignatureShouldBeFalse() {
        assertThat(cryptoService.hmacSignatureIsValid(PAYLOAD, INVALID_SIGNATURE)).isFalse();
    }

    @Test
    void nullParametersValidationShouldBeFalse() {
        assertThat(cryptoService.hmacSignatureIsValid(null, VALID_SIGNATURE)).isFalse();
        assertThat(cryptoService.hmacSignatureIsValid(PAYLOAD, null)).isFalse();
        assertThat(cryptoService.hmacSignatureIsValid(null, null)).isFalse();
    }
}
