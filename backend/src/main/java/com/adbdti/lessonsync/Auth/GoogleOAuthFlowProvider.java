package com.adbdti.lessonsync.Auth;

import com.adbdti.lessonsync.Config.GoogleOAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

/**
 * Owns the single {@link GoogleAuthorizationCodeFlow} used for the server side web flow.
 *
 * <p>The flow is created on first use rather than at startup so that the application still boots
 * (and its non-calendar endpoints still work) when the OAuth client has not been configured yet.
 */
@Component
public class GoogleOAuthFlowProvider {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthFlowProvider.class);

    private static final String SETUP_HINT =
            "Set google.oauth.client-id and google.oauth.client-secret (or the "
                    + "GOOGLE_OAUTH_CLIENT_ID / GOOGLE_OAUTH_CLIENT_SECRET environment variables), "
                    + "or set GOOGLE_OAUTH_CREDENTIALS_JSON to the contents of your downloaded "
                    + "OAuth client JSON, or place credentials.json on the classpath. "
                    + "See README.md for the full setup steps.";

    private final GoogleOAuthProperties properties;

    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private volatile GoogleAuthorizationCodeFlow flow;
    private volatile NetHttpTransport httpTransport;

    public GoogleOAuthFlowProvider(GoogleOAuthProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void warnIfUnconfigured() {
        if (!isConfigured()) {
            log.warn("Google OAuth is not configured, so calendar features will be unavailable. {}", SETUP_HINT);
        }
    }

    public GoogleOAuthProperties getProperties() {
        return properties;
    }

    public JsonFactory getJsonFactory() {
        return jsonFactory;
    }

    public NetHttpTransport getHttpTransport() {
        NetHttpTransport local = httpTransport;
        if (local == null) {
            synchronized (this) {
                local = httpTransport;
                if (local == null) {
                    try {
                        local = GoogleNetHttpTransport.newTrustedTransport();
                    } catch (GeneralSecurityException | IOException e) {
                        throw new IllegalStateException("Unable to create a trusted HTTP transport", e);
                    }
                    httpTransport = local;
                }
            }
        }
        return local;
    }

    /**
     * @throws GoogleOAuthNotConfiguredException if no OAuth client credentials are available
     */
    public GoogleAuthorizationCodeFlow getFlow() {
        GoogleAuthorizationCodeFlow local = flow;
        if (local == null) {
            synchronized (this) {
                local = flow;
                if (local == null) {
                    local = buildFlow();
                    flow = local;
                }
            }
        }
        return local;
    }

    public boolean isConfigured() {
        return properties.hasInlineClientSecrets()
                || properties.hasCredentialsJson()
                || credentialsFileExists();
    }

    private GoogleAuthorizationCodeFlow buildFlow() {
        GoogleClientSecrets clientSecrets = loadClientSecrets();
        try {
            return new GoogleAuthorizationCodeFlow.Builder(
                    getHttpTransport(), jsonFactory, clientSecrets, properties.getScopes())
                    .setDataStoreFactory(
                            new FileDataStoreFactory(new File(properties.getTokenStoreDirectory())))
                    .setAccessType("offline")
                    .build();
        } catch (IOException e) {
            throw new GoogleOAuthNotConfiguredException("Unable to initialise the Google OAuth flow", e);
        }
    }

    private GoogleClientSecrets loadClientSecrets() {
        if (properties.hasInlineClientSecrets()) {
            GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                    .setClientId(properties.getClientId())
                    .setClientSecret(properties.getClientSecret())
                    .setRedirectUris(java.util.List.of(properties.getRedirectUri()));
            return new GoogleClientSecrets().setWeb(details);
        }

        if (properties.hasCredentialsJson()) {
            try {
                return GoogleClientSecrets.load(
                        jsonFactory, new InputStreamReader(
                                new java.io.ByteArrayInputStream(
                                        properties.getCredentialsJson()
                                                .getBytes(java.nio.charset.StandardCharsets.UTF_8))));
            } catch (IOException e) {
                throw new GoogleOAuthNotConfiguredException(
                        "Unable to parse GOOGLE_OAUTH_CREDENTIALS_JSON. " + SETUP_HINT, e);
            }
        }

        try (InputStream in = openCredentialsFile()) {
            if (in == null) {
                throw new GoogleOAuthNotConfiguredException("Google OAuth is not configured. " + SETUP_HINT);
            }
            return GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
        } catch (IOException e) {
            throw new GoogleOAuthNotConfiguredException(
                    "Unable to read " + properties.getCredentialsFile() + ". " + SETUP_HINT, e);
        }
    }

    private InputStream openCredentialsFile() {
        return GoogleOAuthFlowProvider.class.getResourceAsStream(properties.getCredentialsFile());
    }

    private boolean credentialsFileExists() {
        try (InputStream in = openCredentialsFile()) {
            return in != null;
        } catch (IOException e) {
            return false;
        }
    }
}
