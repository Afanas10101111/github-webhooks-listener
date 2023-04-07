package com.github.afanas10101111.gwl.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.afanas10101111.gwl.exeption.HmacSignatureValidationException;
import com.github.afanas10101111.gwl.exeption.ScriptFileAccessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler({
            HttpMediaTypeNotSupportedException.class,
            HttpMessageNotReadableException.class,
            JsonProcessingException.class,
            HmacSignatureValidationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void handleBadRequestExceptions(Exception ex) {
        log.warn("handleBadRequestExceptions -> {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleScriptFileAccessException(ScriptFileAccessException ex) {
        log.warn("handleScriptFileAccessException -> {}", ex.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    void handleUnexpectedException(Exception ex) {
        log.error(
                "handleUnexpectedException -> {}; with stackTrace: {}",
                ex.getMessage(),
                Arrays.toString(ex.getStackTrace())
        );
    }
}
