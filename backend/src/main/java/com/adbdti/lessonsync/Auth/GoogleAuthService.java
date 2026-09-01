package com.adbdti.lessonsync.Auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Handles the Google OAuth authorization code exchange and hands out
 * authorized Calendar clients. Credentials are stored per user key (the HTTP
 * session id) and are refreshed automatically via the stored refresh token.
 */
@Service
public class GoogleAuthService {

    private static final String APPLICATION_NAME = "LessonSync";

    private final GoogleAuthorizationCodeFlow flow;
    private final NetHttpTransport httpTransport;
    private final String redirectUri;

    public GoogleAuthService(GoogleAuthorizationCodeFlow flow,
                             NetHttpTransport httpTransport,
                             @Value("${google.oauth.redirect-uri}") String redirectUri) {
        this.flow = flow;
        this.httpTransport = httpTransport;
        this.redirectUri = redirectUri;
    }

    public String buildAuthorizationUrl(String state) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(state)
                .build();
    }

    /**
     * Exchanges the authorization code returned by Google for access and
     * refresh tokens and persists them under the given user key.
     */
    public void exchangeAndStoreCode(String code, String userKey) throws IOException {
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                .setRedirectUri(redirectUri)
                .execute();
        flow.createAndStoreCredential(tokenResponse, userKey);
    }

    public boolean isAuthorized(String userKey) {
        try {
            return loadCredential(userKey) != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns a Calendar client that authenticates as the given user and
     * transparently refreshes expired access tokens.
     */
    public Calendar getCalendarClient(String userKey) {
        Credential credential;
        try {
            credential = loadCredential(userKey);
        } catch (IOException e) {
            throw new GoogleAuthRequiredException(e);
        }
        if (credential == null) {
            throw new GoogleAuthRequiredException();
        }
        return new Calendar.Builder(httpTransport, flow.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public void revoke(String userKey) throws IOException {
        flow.getCredentialDataStore().delete(userKey);
    }

    private Credential loadCredential(String userKey) throws IOException {
        Credential credential = flow.loadCredential(userKey);
        if (credential == null) {
            return null;
        }
        // Without a refresh token an expired credential cannot be renewed,
        // so drop it and make the user go through consent again.
        boolean expired = credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60;
        if (credential.getRefreshToken() == null && expired) {
            revoke(userKey);
            return null;
        }
        return credential;
    }
}
