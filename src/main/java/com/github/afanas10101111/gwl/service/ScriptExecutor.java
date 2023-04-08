package com.github.afanas10101111.gwl.service;

import com.github.afanas10101111.gwl.service.exception.ScriptFileAccessException;

public interface ScriptExecutor {
    void execute(String scriptName) throws ScriptFileAccessException;
}
