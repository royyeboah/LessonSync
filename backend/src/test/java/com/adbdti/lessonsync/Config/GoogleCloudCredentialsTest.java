package com.adbdti.lessonsync.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleCloudCredentialsTest {

    @TempDir
    Path tempDir;

    @Test
    void writeJsonToTempFileIsANoOpWhenNothingIsSupplied() {
        assertThat(GoogleCloudCredentials.writeJsonToTempFile(null)).isEmpty();
        assertThat(GoogleCloudCredentials.writeJsonToTempFile("  ")).isEmpty();
    }

    @Test
    void writeJsonToTempFileMaterialisesTheJson() throws Exception {
        String json = serviceAccountJson();

        Optional<Path> written = GoogleCloudCredentials.writeJsonToTempFile(json);

        assertThat(written).isPresent();
        assertThat(Files.readString(written.get())).contains("vertex@demo-project.iam.gserviceaccount.com");
    }

    @Test
    void fromJsonReadsAServiceAccount() throws Exception {
        GoogleCredentials credentials = GoogleCloudCredentials.fromJson(serviceAccountJson());

        assertThat(credentials).isInstanceOf(ServiceAccountCredentials.class);
        assertThat(((ServiceAccountCredentials) credentials).getClientEmail())
                .isEqualTo("vertex@demo-project.iam.gserviceaccount.com");
    }

    @Test
    void loadPrefersInlineJsonOverAFilePath() throws Exception {
        Path other = tempDir.resolve("other.json");
        Files.writeString(other, serviceAccountJson("other@example.com"));

        GoogleCredentials credentials = GoogleCloudCredentials.load(serviceAccountJson(), other.toString());

        assertThat(((ServiceAccountCredentials) credentials).getClientEmail())
                .isEqualTo("vertex@demo-project.iam.gserviceaccount.com");
    }

    @Test
    void loadReadsACredentialsFileWhenJsonIsAbsent() throws Exception {
        Path file = tempDir.resolve("adc.json");
        Files.writeString(file, serviceAccountJson());

        GoogleCredentials credentials = GoogleCloudCredentials.load(null, file.toString());

        assertThat(((ServiceAccountCredentials) credentials).getClientEmail())
                .isEqualTo("vertex@demo-project.iam.gserviceaccount.com");
    }

    @Test
    void loadWithoutAnySourceAsksForApplicationDefaultCredentials() {
        assertThatThrownBy(() -> GoogleCloudCredentials.load(null, null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Application Default Credentials");
    }

    private static String serviceAccountJson() throws NoSuchAlgorithmException {
        return serviceAccountJson("vertex@demo-project.iam.gserviceaccount.com");
    }

    private static String serviceAccountJson(String clientEmail) throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        String pem = "-----BEGIN PRIVATE KEY-----\\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(pair.getPrivate().getEncoded())
                        .replace("\n", "\\n")
                + "\\n-----END PRIVATE KEY-----\\n";

        return """
                {
                  "type": "service_account",
                  "project_id": "demo-project",
                  "private_key_id": "abc123",
                  "private_key": "%s",
                  "client_email": "%s",
                  "client_id": "1234567890",
                  "token_uri": "https://oauth2.googleapis.com/token"
                }
                """.formatted(pem, clientEmail);
    }
}
