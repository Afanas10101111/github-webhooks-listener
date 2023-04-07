package com.github.afanas10101111.gwl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.afanas10101111.gwl.dto.PushPayload;
import com.github.afanas10101111.gwl.exeption.HmacSignatureValidationException;
import com.github.afanas10101111.gwl.service.CryptoService;
import com.github.afanas10101111.gwl.service.ScriptExecutor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(WebhookController.GLOBAL_MAPPING)
public class WebhookController {
    public static final String GLOBAL_MAPPING = "/gwl/v1";
    public static final String X_HUB_SIGNATURE_256 = "x-hub-signature-256";
    public static final String SIGNATURE_PREFIX_REGEX = "^sha256=";
    public static final String ERROR = X_HUB_SIGNATURE_256 + " is missing";

    private final ScriptExecutor scriptExecutor;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/push/{branchName}/{scriptName}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void processPushEvent(
            @PathVariable String branchName,
            @PathVariable String scriptName,
            @RequestBody String payload,
            HttpServletRequest request
    ) throws JsonProcessingException {
        validateSignature(request, payload);
        PushPayload pushPayload = objectMapper.readValue(payload, PushPayload.class);
        log.info("processPushEvent -> branchName={}; scriptName={}; ref={}", branchName, scriptName, pushPayload.ref());
        if (pushPayload.ref() != null && pushPayload.ref().endsWith("/" + branchName)) {
            scriptExecutor.execute(scriptName);
        }
    }

    private void validateSignature(HttpServletRequest request, String payload) {
        String signature = request.getHeader(X_HUB_SIGNATURE_256);
        if (signature == null) {
            throw new HmacSignatureValidationException(ERROR);
        }
        cryptoService.validateHmacSignature(payload, signature.replaceFirst(SIGNATURE_PREFIX_REGEX, ""));
    }
}
