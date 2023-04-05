package com.github.afanas10101111.gwl.controller;

import com.github.afanas10101111.gwl.dto.PushPayload;
import com.github.afanas10101111.gwl.service.ScriptExecutor;
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

    private final ScriptExecutor scriptExecutor;

    @PostMapping(value = "/push/{branchName}/{scriptName}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void handlePushEvent(
            @PathVariable String branchName, @PathVariable String scriptName, @RequestBody PushPayload pushPayload
    ) {
        log.info("runScript -> branchName={}; scriptName={}; ref={}", branchName, scriptName, pushPayload.ref());
        if (pushPayload.ref() != null && pushPayload.ref().endsWith("/" + branchName)) {
            scriptExecutor.execute(scriptName);
        }
    }
}
