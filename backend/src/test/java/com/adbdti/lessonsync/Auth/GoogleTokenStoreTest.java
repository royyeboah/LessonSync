package com.adbdti.lessonsync.Auth;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the refresh token really is persisted, which is what lets the server renew access on
 * its own instead of anyone pasting a token into the source.
 */
class GoogleTokenStoreTest {

    @TempDir
    Path tokenDirectory;

    @Test
    void roundTripsACredentialThroughTheStore() throws IOException {
        GoogleAuthorizationCodeFlow flow = newFlow();

        flow.createAndStoreCredential(new TokenResponse()
                .setAccessToken("access-token")
                .setRefreshToken("refresh-token")
                .setExpiresInSeconds(3600L), "google-subject-1");

        Credential credential = flow.loadCredential("google-subject-1");

        assertThat(credential).isNotNull();
        assertThat(credential.getAccessToken()).isEqualTo("access-token");
        assertThat(credential.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(credential.getExpiresInSeconds()).isPositive();
    }

    /**
     * A second flow over the same directory stands in for a restarted application.
     */
    @Test
    void credentialsOutliveTheProcessThatWroteThem() throws IOException {
        newFlow().createAndStoreCredential(new TokenResponse()
                .setAccessToken("access-token")
                .setRefreshToken("refresh-token")
                .setExpiresInSeconds(3600L), "google-subject-1");

        Credential credential = newFlow().loadCredential("google-subject-1");

        assertThat(credential).isNotNull();
        assertThat(credential.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void keepsAccountsApart() throws IOException {
        GoogleAuthorizationCodeFlow flow = newFlow();
        flow.createAndStoreCredential(new TokenResponse().setRefreshToken("refresh-1"), "subject-1");
        flow.createAndStoreCredential(new TokenResponse().setRefreshToken("refresh-2"), "subject-2");

        assertThat(flow.loadCredential("subject-1").getRefreshToken()).isEqualTo("refresh-1");
        assertThat(flow.loadCredential("subject-2").getRefreshToken()).isEqualTo("refresh-2");
    }

    @Test
    void reportsNoCredentialForAnUnknownAccount() throws IOException {
        assertThat(newFlow().loadCredential("never-signed-in")).isNull();
    }

    @Test
    void forgettingAnAccountRemovesItsCredential() throws IOException {
        GoogleAuthorizationCodeFlow flow = newFlow();
        flow.createAndStoreCredential(new TokenResponse().setRefreshToken("refresh-1"), "subject-1");

        flow.getCredentialDataStore().delete("subject-1");

        assertThat(newFlow().loadCredential("subject-1")).isNull();
    }

    private GoogleAuthorizationCodeFlow newFlow() throws IOException {
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(
                new GoogleClientSecrets.Details()
                        .setClientId("test-client-id")
                        .setClientSecret("test-client-secret"));

        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                List.of("https://www.googleapis.com/auth/calendar"))
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokenDirectory.toString())))
                .setMethod(BearerToken.authorizationHeaderAccessMethod())
                .setAccessType("offline")
                .build();
    }
}
