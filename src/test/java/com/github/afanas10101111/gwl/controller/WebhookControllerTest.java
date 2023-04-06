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
@MockBean(classes = CryptoService.class)
class WebhookControllerTest {
    public static final String PUSH_TO_MAIN_MAPPING = WebhookController.GLOBAL_MAPPING + "/push/main/";
    public static final String SCRIPT_NAME = "scriptName";
    public static final String REQ_WITH_REF_TO_MAIN_LOCATION = "classpath:__files/reqWithRefToMain.json";
    public static final String REQ_WITH_REF_TO_BRANCH_LOCATION = "classpath:__files/reqWithRefToBranch.json";
    public static final String REQ_WITHOUT_REF_LOCATION = "classpath:__files/reqWithoutRef.json";
    public static final String REQ_WITHOUT_BODY_LOCATION = "classpath:__files/reqWithoutBody.json";
    public static final String REQ_WITH_NOT_JSON_LOCATION = "classpath:__files/reqWithNotJson.txt";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScriptExecutor scriptExecutorMock;

    @Test
    void requestWithPushPayloadRefToCorrectBranchShouldExecuteScript() throws Exception {
        performPushEvent(APPLICATION_JSON, REQ_WITH_REF_TO_MAIN_LOCATION)
                .andDo(print())
                .andExpect(status().isOk());
        verify(scriptExecutorMock, only()).execute(SCRIPT_NAME);
    }

    @Test
    void requestWithPushPayloadRefToIncorrectBranchShouldNotExecuteScript() throws Exception {
        performPushEvent(APPLICATION_JSON, REQ_WITH_REF_TO_BRANCH_LOCATION)
                .andDo(print())
                .andExpect(status().isOk());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void requestWithoutRefShouldNotExecuteScript() throws Exception {
        performPushEvent(APPLICATION_JSON, REQ_WITHOUT_REF_LOCATION)
                .andDo(print())
                .andExpect(status().isOk());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void requestWithoutBodyShouldBeHandled() throws Exception {
        performPushEvent(APPLICATION_JSON, REQ_WITHOUT_BODY_LOCATION)
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(scriptExecutorMock, never()).execute(anyString());
    }

    @Test
    void httpMediaTypeNotSupportedExceptionShouldBeHandled() throws Exception {
        performPushEvent(APPLICATION_FORM_URLENCODED, REQ_WITHOUT_BODY_LOCATION)
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonProcessingExceptionShouldBeHandled() throws Exception {
        performPushEvent(APPLICATION_JSON, REQ_WITH_NOT_JSON_LOCATION)
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void scriptFileAccessExceptionShouldBeHandled() throws Exception {
        doThrow(new ScriptFileAccessException("")).when(scriptExecutorMock).execute(anyString());
        performPushEvent(APPLICATION_JSON, REQ_WITH_REF_TO_MAIN_LOCATION)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void unexpectedExceptionShouldBeHandled() throws Exception {
        doThrow(new NullPointerException()).when(scriptExecutorMock).execute(anyString());
        performPushEvent(APPLICATION_JSON, REQ_WITH_REF_TO_MAIN_LOCATION)
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    private ResultActions performPushEvent(MediaType contentType, String requestFileLocation) throws Exception {
        return mockMvc.perform(post(PUSH_TO_MAIN_MAPPING + SCRIPT_NAME)
                .contentType(contentType)
                .content(Files.readAllBytes(ResourceUtils.getFile(requestFileLocation).toPath())));
    }
}
