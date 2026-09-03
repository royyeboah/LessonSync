package com.adbdti.lessonsync.Auth;

import com.adbdti.lessonsync.Config.GoogleOAuthProperties;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Runs the Google OAuth 2.0 authorization code flow for a web client.
 *
 * <p>Each user is sent to Google's consent screen, and the code that comes back on the redirect is
 * exchanged for tokens that are stored per Google account. Access tokens are refreshed
 * automatically from the stored refresh token, so no token ever has to be pasted into the code.
 */
@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    private static final String REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke";

    /**
     * Access tokens expiring within this window are refreshed up front rather than mid request.
     */
    private static final long REFRESH_THRESHOLD_SECONDS = 60;

    private final GoogleOAuthFlowProvider flowProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleAuthService(GoogleOAuthFlowProvider flowProvider) {
        this.flowProvider = flowProvider;
    }

    public boolean isConfigured() {
        return flowProvider.isConfigured();
    }

    public String newState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Builds the Google consent screen URL the browser should be sent to.
     *
     * @param state opaque value echoed back on the callback, used to defend against CSRF
     */
    public String buildAuthorizationUrl(String state) {
        GoogleOAuthProperties properties = flowProvider.getProperties();
        return flowProvider.getFlow()
                .newAuthorizationUrl()
                .setRedirectUri(properties.getRedirectUri())
                .setState(state)
                // Guarantees a refresh token even if this account has consented before.
                .set("prompt", "consent")
                .set("include_granted_scopes", true)
                .build();
    }

    /**
     * Exchanges an authorization code for tokens and persists them against the Google account.
     */
    public GoogleAccount exchangeCode(String code) throws IOException {
        GoogleAuthorizationCodeFlow flow = flowProvider.getFlow();
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(flowProvider.getProperties().getRedirectUri())
                .execute();

        GoogleAccount account = readAccount(tokenResponse);

        if (tokenResponse.getRefreshToken() == null) {
            // Keep whatever refresh token we already hold rather than overwriting it with null.
            Credential existing = flow.loadCredential(account.userId());
            if (existing != null && existing.getRefreshToken() != null) {
                tokenResponse.setRefreshToken(existing.getRefreshToken());
            } else {
                log.warn("Google did not return a refresh token for account {}; "
                        + "calendar access will stop working when the access token expires", account.userId());
            }
        }

        flow.createAndStoreCredential(tokenResponse, account.userId());
        return account;
    }

    /**
     * Loads the stored credential for a user, refreshing the access token when it is about to
     * expire.
     *
     * @throws GoogleAuthRequiredException when there is nothing usable stored for this user
     */
    public Credential loadCredential(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new GoogleAuthRequiredException("Not signed in with Google.");
        }

        Credential credential;
        try {
            credential = flowProvider.getFlow().loadCredential(userId);
        } catch (IOException e) {
            throw new GoogleAuthRequiredException("Could not read the stored Google credential: " + e.getMessage());
        }

        if (credential == null) {
            throw new GoogleAuthRequiredException("No Google credential is stored for this session.");
        }

        Long expiresIn = credential.getExpiresInSeconds();
        boolean needsRefresh = credential.getAccessToken() == null
                || (expiresIn != null && expiresIn <= REFRESH_THRESHOLD_SECONDS);

        if (needsRefresh) {
            if (credential.getRefreshToken() == null) {
                throw new GoogleAuthRequiredException(
                        "The Google access token has expired and no refresh token is available.");
            }
            try {
                if (!credential.refreshToken()) {
                    throw new GoogleAuthRequiredException("Google rejected the stored refresh token.");
                }
            } catch (IOException e) {
                throw new GoogleAuthRequiredException("Could not refresh the Google access token: " + e.getMessage());
            }
        }

        return credential;
    }

    public boolean hasStoredCredential(String userId) {
        if (userId == null || userId.isBlank() || !isConfigured()) {
            return false;
        }
        try {
            Credential credential = flowProvider.getFlow().loadCredential(userId);
            return credential != null
                    && (credential.getRefreshToken() != null || credential.getAccessToken() != null);
        } catch (IOException | GoogleOAuthNotConfiguredException e) {
            return false;
        }
    }

    /**
     * Revokes the tokens at Google and forgets them locally.
     */
    public void disconnect(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        GoogleAuthorizationCodeFlow flow;
        try {
            flow = flowProvider.getFlow();
        } catch (GoogleOAuthNotConfiguredException e) {
            return;
        }

        try {
            Credential credential = flow.loadCredential(userId);
            if (credential != null) {
                String token = credential.getRefreshToken() != null
                        ? credential.getRefreshToken()
                        : credential.getAccessToken();
                revoke(token);
            }
            flow.getCredentialDataStore().delete(userId);
        } catch (IOException e) {
            log.warn("Failed to fully disconnect Google account {}: {}", userId, e.getMessage());
        }
    }

    private void revoke(String token) {
        if (token == null) {
            return;
        }
        try {
            GenericUrl url = new GenericUrl(REVOKE_ENDPOINT);
            url.set("token", token);
            flowProvider.getHttpTransport()
                    .createRequestFactory()
                    .buildPostRequest(url, null)
                    .execute()
                    .disconnect();
        } catch (HttpResponseException e) {
            // Already revoked or expired tokens come back as 400; nothing left to do.
            log.debug("Google rejected the token revocation request: {}", e.getStatusMessage());
        } catch (IOException e) {
            log.warn("Could not reach Google to revoke the token: {}", e.getMessage());
        }
    }

    private GoogleAccount readAccount(GoogleTokenResponse tokenResponse) throws IOException {
        GoogleIdToken idToken = tokenResponse.parseIdToken();
        if (idToken == null) {
            throw new IOException("Google did not return an ID token; check that the "
                    + "'openid' and 'email' scopes are requested.");
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return new GoogleAccount(payload.getSubject(), payload.getEmail());
    }
}
