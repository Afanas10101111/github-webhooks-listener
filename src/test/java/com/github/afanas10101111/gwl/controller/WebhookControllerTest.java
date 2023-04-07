package com.github.afanas10101111.gwl.controller;

import com.github.afanas10101111.gwl.exeption.ScriptFileAccessException;
import com.github.afanas10101111.gwl.service.CryptoService;
import com.github.afanas10101111.gwl.service.ScriptExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {
    public static final String PUSH_TO_MAIN_MAPPING = WebhookController.GLOBAL_MAPPING + "/push/main/";
    public static final String SCRIPT_NAME = "scriptName";
    public static final String REQ_WITH_REF_TO_MAIN_LOCATION = "classpath:__files/reqWithRefToMain.json";
    public static final String REQ_WITH_REF_TO_BRANCH_LOCATION = "classpath:__files/reqWithRefToBranch.json";
    public static final String REQ_WITHOUT_REF_LOCATION = "classpath:__files/reqWithoutRef.json";
    public static final String REQ_WITH_NOT_JSON_LOCATION = "classpath:__files/reqWithNotJson.txt";
    public static final String SIGNATURE_WITH_PREFIX = "sha256=1234";
    public static final String SIGNATURE = "1234";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScriptExecutor scriptExecutorMock;

    @MockBean
    private CryptoService cryptoService;

    @Test
    void requestWithPushPayloadRefToCorrectBranchShouldExecuteScript() throws Exception {
        String payload = readFile(REQ_WITH_REF_TO_MAIN_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isOk());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
        verify(scriptExecutorMock, only()).execute(SCRIPT_NAME);
    }

    @Test
    void requestWithPushPayloadRefToIncorrectBranchShouldNotExecuteScript() throws Exception {
        String payload = readFile(REQ_WITH_REF_TO_BRANCH_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isOk());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void requestWithoutRefShouldNotExecuteScript() throws Exception {
        String payload = readFile(REQ_WITHOUT_REF_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isOk());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void requestWithoutSignatureHeaderShouldBeHandled() throws Exception {
        String payload = readFile(REQ_WITH_REF_TO_MAIN_LOCATION);
        performPushEvent(APPLICATION_JSON, SIGNATURE_WITH_PREFIX, payload)
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(cryptoService, never()).validateHmacSignature(anyString(), anyString());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void requestWithoutBodyShouldBeHandled() throws Exception {
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, "")
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(cryptoService, never()).validateHmacSignature(anyString(), anyString());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void httpMediaTypeNotSupportedExceptionShouldBeHandled() throws Exception {
        String payload = readFile(REQ_WITH_REF_TO_MAIN_LOCATION);
        performPushEvent(APPLICATION_FORM_URLENCODED, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(cryptoService, never()).validateHmacSignature(anyString(), anyString());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void jsonProcessingExceptionShouldBeHandled() throws Exception {
        String payload = readFile(REQ_WITH_NOT_JSON_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void scriptFileAccessExceptionShouldBeHandled() throws Exception {
        doThrow(new ScriptFileAccessException("")).when(scriptExecutorMock).execute(anyString());

        String payload = readFile(REQ_WITH_REF_TO_MAIN_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isNotFound());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
    }

    @Test
    void unexpectedExceptionShouldBeHandled() throws Exception {
        doThrow(new NullPointerException()).when(scriptExecutorMock).execute(anyString());

        String payload = readFile(REQ_WITH_REF_TO_MAIN_LOCATION);
        performPushEvent(APPLICATION_JSON, WebhookController.X_HUB_SIGNATURE_256, payload)
                .andDo(print())
                .andExpect(status().isInternalServerError());
        verify(cryptoService, only()).validateHmacSignature(payload, SIGNATURE);
    }

    private String readFile(String requestFileLocation) throws IOException {
        return String.join(
                System.lineSeparator(), Files.readAllLines(ResourceUtils.getFile(requestFileLocation).toPath())
        );
    }

    private ResultActions performPushEvent(
            MediaType contentType, String signatureHeaderName, String payload
    ) throws Exception {
        return mockMvc.perform(post(PUSH_TO_MAIN_MAPPING + SCRIPT_NAME)
                .contentType(contentType)
                .header(signatureHeaderName, SIGNATURE_WITH_PREFIX)
                .content(payload));
    }
}
