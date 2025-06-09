package com.deeptechhub.apigateway.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class SecretFileReader {
    public String readSecret(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath)).trim();
    }
}
