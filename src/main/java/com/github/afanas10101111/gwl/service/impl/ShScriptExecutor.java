package com.github.afanas10101111.gwl.service.impl;

import com.github.afanas10101111.gwl.service.exception.ScriptFileAccessException;
import com.github.afanas10101111.gwl.service.ScriptExecutor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Service
public class ShScriptExecutor implements ScriptExecutor {
    public static final String BIN_SH = "/bin/sh";
    public static final String FILE_EXTENSION = ".sh";
    public static final String SCRIPT_FILE_NOT_FOUND_FORMAT = "file with name \"%s\" not found";

    @Value("${script.catalog_name}")
    private String scriptCatalogName;
    private Path appFolderPath;

    @PostConstruct
    private void appPathConstruct() throws IOException {
        appFolderPath = Paths.get(URI.create(
                ShScriptExecutor.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toString()
                        .replaceAll("(^jar:)|([^/]+\\.jar.*$)", "")
        ).resolve(scriptCatalogName));
        try {
            Files.createDirectory(appFolderPath);
        } catch (FileAlreadyExistsException ex) {
            log.info("appPathConstruct -> {} already exist", appFolderPath);
        }
    }

    @Override
    public void execute(String scriptName) throws ScriptFileAccessException {
        try {
            String scriptFilePath = getScriptFilePath(scriptName);
            log.info("execute -> {}", scriptFilePath);
            Runtime.getRuntime().exec(new String[]{BIN_SH, scriptFilePath});
        } catch (IOException ex) {
            throw new ScriptFileAccessException(ex.getMessage());
        }
    }

    private String getScriptFilePath(String scriptName) throws IOException {
        try (Stream<Path> stream = Files.list(appFolderPath)) {
            return stream
                    .filter(f -> !Files.isDirectory(f))
                    .map(f -> f.getFileName().toString())
                    .filter(f -> f.equals(scriptName + FILE_EXTENSION))
                    .map(f -> appFolderPath.resolve(f).toString())
                    .findFirst()
                    .orElseThrow(
                            () -> new FileNotFoundException(String.format(SCRIPT_FILE_NOT_FOUND_FORMAT, scriptName))
                    );
        }
    }
}
