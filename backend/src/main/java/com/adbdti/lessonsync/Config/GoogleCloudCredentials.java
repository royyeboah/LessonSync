package com.adbdti.lessonsync.Config;

import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Loads Vertex AI / GCP Application Default Credentials from the host environment.
 *
 * <p>Local development can keep using {@code gcloud auth application-default login} or a
 * {@code GOOGLE_APPLICATION_CREDENTIALS} file path. Hosts such as Railway only accept secrets as
 * environment variables, so this also understands {@code GOOGLE_APPLICATION_CREDENTIALS_JSON}.
 */
public final class GoogleCloudCredentials {

    private static final Logger log = LoggerFactory.getLogger(GoogleCloudCredentials.class);

    public static final String JSON_ENV = "GOOGLE_APPLICATION_CREDENTIALS_JSON";

    public static final String FILE_ENV = "GOOGLE_APPLICATION_CREDENTIALS";

    private GoogleCloudCredentials() {
    }

    /**
     * Writes {@code GOOGLE_APPLICATION_CREDENTIALS_JSON} to a temp file and points
     * {@code GOOGLE_APPLICATION_CREDENTIALS} at it when the file path is not already set.
     * Safe to call more than once.
     */
    public static Optional<Path> installFromEnvironment() {
        return writeJsonToTempFile(System.getenv(JSON_ENV));
    }

    public static Optional<Path> writeJsonToTempFile(String json) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        String existing = System.getenv(FILE_ENV);
        if (existing != null && !existing.isBlank() && Files.isRegularFile(Path.of(existing))) {
            return Optional.of(Path.of(existing));
        }

        try {
            Path file = Files.createTempFile("gcp-application-credentials-", ".json");
            Files.writeString(file, json, StandardCharsets.UTF_8);
            file.toFile().deleteOnExit();
            log.info("Wrote Google Cloud credentials from {} to {}", JSON_ENV, file);
            return Optional.of(file);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to materialise " + JSON_ENV + " as a credentials file", e);
        }
    }

    public static GoogleCredentials load() throws IOException {
        return load(System.getenv(JSON_ENV), System.getenv(FILE_ENV));
    }

    public static GoogleCredentials load(String json, String filePath) throws IOException {
        if (json != null && !json.isBlank()) {
            return fromJson(json);
        }

        if (filePath != null && !filePath.isBlank()) {
            try (InputStream in = Files.newInputStream(Path.of(filePath))) {
                return GoogleCredentials.fromStream(in);
            }
        }

        return GoogleCredentials.getApplicationDefault();
    }

    public static GoogleCredentials fromJson(String json) throws IOException {
        return GoogleCredentials.fromStream(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }
}
