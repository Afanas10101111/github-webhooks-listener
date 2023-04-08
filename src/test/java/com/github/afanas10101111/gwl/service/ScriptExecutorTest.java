package com.github.afanas10101111.gwl.service;

import com.github.afanas10101111.gwl.service.exception.ScriptFileAccessException;
import com.github.afanas10101111.gwl.service.impl.ShScriptExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ShScriptExecutor.class)
class ScriptExecutorTest {
    public static final String APP_FOLDER_PATH_FIELD_NAME = "appFolderPath";
    public static final String NOT_EXISTING_FILE_NAME = "notExistingFileName";
    public static final String EXISTING_FILE_NAME = "existingFileName";

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Test
    void executionOfNotExistingFileShouldCauseException() {
        ScriptFileAccessException scriptFileAccessException = assertThrows(
                ScriptFileAccessException.class, () -> scriptExecutor.execute(NOT_EXISTING_FILE_NAME)
        );
        assertThat(scriptFileAccessException.getMessage())
                .isEqualTo(String.format(ShScriptExecutor.SCRIPT_FILE_NOT_FOUND_FORMAT, NOT_EXISTING_FILE_NAME));
    }

    @Test
    void executionExistingFileShouldExecRuntime()
            throws NoSuchFieldException, IllegalAccessException, IOException, ScriptFileAccessException {
        Field appFolderPathField = ShScriptExecutor.class.getDeclaredField(APP_FOLDER_PATH_FIELD_NAME);
        appFolderPathField.setAccessible(true);
        Path appFolderPath = (Path) appFolderPathField.get(scriptExecutor);
        Path path = appFolderPath.resolve(EXISTING_FILE_NAME + ShScriptExecutor.FILE_EXTENSION);

        Files.write(path, new byte[0]);

        try (MockedStatic<Runtime> staticRuntimeMock = mockStatic(Runtime.class)) {
            Runtime runtimeMock = mock(Runtime.class);
            staticRuntimeMock.when(Runtime::getRuntime).thenReturn(runtimeMock);

            scriptExecutor.execute(EXISTING_FILE_NAME);
            verify(runtimeMock, times(1))
                    .exec(new String[]{ShScriptExecutor.BIN_SH, path.toString()});
        }

        Files.delete(path);
    }
}
