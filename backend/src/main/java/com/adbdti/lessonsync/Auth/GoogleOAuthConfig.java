package com.adbdti.lessonsync.Auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Configures the server-side (web) Google OAuth 2.0 authorization code flow.
 *
 * The OAuth client is resolved from the GOOGLE_OAUTH_CLIENT_ID / GOOGLE_OAUTH_CLIENT_SECRET
 * environment variables (see application.properties), falling back to a credentials.json
 * file on the classpath. Granted tokens (including refresh tokens) are persisted in the
 * tokens directory so users never have to paste access tokens by hand.
 */
@Configuration
public class GoogleOAuthConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Value("${google.oauth.client-id:}")
    private String clientId;

    @Value("${google.oauth.client-secret:}")
    private String clientSecret;

    @Value("${google.oauth.tokens-directory:tokens}")
    private String tokensDirectory;

    @Bean
    public NetHttpTransport googleHttpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(NetHttpTransport httpTransport) throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, loadClientSecrets(), SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectory)))
                // "offline" + "force" guarantee Google issues a refresh token, so access
                // tokens can be renewed automatically without user interaction.
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }

    private GoogleClientSecrets loadClientSecrets() throws IOException {
        if (!clientId.isBlank() && !clientSecret.isBlank()) {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setAuthUri("https://accounts.google.com/o/oauth2/auth")
                    .setTokenUri("https://oauth2.googleapis.com/token");
            return new GoogleClientSecrets().setWeb(details);
        }

        InputStream in = GoogleOAuthConfig.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IllegalStateException(
                    "Google OAuth client is not configured. Either set the GOOGLE_OAUTH_CLIENT_ID and " +
                    "GOOGLE_OAUTH_CLIENT_SECRET environment variables, or place the OAuth client's " +
                    "credentials.json (downloaded from the Google Cloud console) in src/main/resources.");
        }
        return GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in, StandardCharsets.UTF_8));
    }
}
