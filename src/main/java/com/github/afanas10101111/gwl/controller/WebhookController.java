package com.github.afanas10101111.gwl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.afanas10101111.gwl.dto.PushPayload;
import com.github.afanas10101111.gwl.controller.exeption.InvalidSignatureException;
import com.github.afanas10101111.gwl.service.CryptoService;
import com.github.afanas10101111.gwl.service.ScriptExecutor;
import com.github.afanas10101111.gwl.service.exception.ScriptFileAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    private final ScriptExecutor scriptExecutor;
    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/push/{branchName}/{scriptName}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void processPushEvent(
            @PathVariable String branchName,
            @PathVariable String scriptName,
            @RequestHeader(X_HUB_SIGNATURE_256) String signature,
            @RequestBody String payload
    ) throws JsonProcessingException, ScriptFileAccessException {
        if (!cryptoService.hmacSignatureIsValid(payload, signature.replaceFirst(SIGNATURE_PREFIX_REGEX, ""))) {
            throw new InvalidSignatureException();
        }
        PushPayload pushPayload = objectMapper.readValue(payload, PushPayload.class);

        log.info("processPushEvent -> branchName={}; scriptName={}; ref={}", branchName, scriptName, pushPayload.ref());
        if (pushPayload.ref() != null && pushPayload.ref().endsWith("/" + branchName)) {
            scriptExecutor.execute(scriptName);
        }
    }
}
