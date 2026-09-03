package com.adbdti.lessonsync.Config;

import com.google.api.services.calendar.CalendarScopes;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Settings for the Google OAuth 2.0 authorization code flow.
 *
 * <p>The client id and secret come from the OAuth client you create under
 * "APIs &amp; Services &gt; Credentials" in the Google Cloud console. They can be supplied either
 * through {@code application.properties} or through the {@code GOOGLE_OAUTH_CLIENT_ID} /
 * {@code GOOGLE_OAUTH_CLIENT_SECRET} environment variables. When neither is set, the legacy
 * {@code credentials.json} on the classpath is used as a fallback.
 */
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {

    private String clientId;

    private String clientSecret;

    /**
     * Must be registered verbatim as an authorized redirect URI on the OAuth client.
     */
    private String redirectUri = "http://localhost:8080/auth/google/callback";

    /**
     * Where the browser is sent once the callback has been processed.
     */
    private String successRedirectUri = "http://localhost:4200/";

    private String failureRedirectUri = "http://localhost:4200/";

    private List<String> scopes = List.of(CalendarScopes.CALENDAR, "openid", "email", "profile");

    /**
     * Classpath location of the downloaded OAuth client secret file, used only when
     * {@link #clientId} and {@link #clientSecret} are not set.
     */
    private String credentialsFile = "/credentials.json";

    /**
     * Directory in which refresh tokens are persisted, so that a restart does not force every
     * student to grant consent again. Keep it out of version control.
     */
    private String tokenStoreDirectory = "tokens";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getSuccessRedirectUri() {
        return successRedirectUri;
    }

    public void setSuccessRedirectUri(String successRedirectUri) {
        this.successRedirectUri = successRedirectUri;
    }

    public String getFailureRedirectUri() {
        return failureRedirectUri;
    }

    public void setFailureRedirectUri(String failureRedirectUri) {
        this.failureRedirectUri = failureRedirectUri;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getCredentialsFile() {
        return credentialsFile;
    }

    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public String getTokenStoreDirectory() {
        return tokenStoreDirectory;
    }

    public void setTokenStoreDirectory(String tokenStoreDirectory) {
        this.tokenStoreDirectory = tokenStoreDirectory;
    }

    public boolean hasInlineClientSecrets() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
