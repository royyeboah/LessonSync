package com.adbdti.lessonsync.Auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class GoogleAccessTokenService {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public GoogleAccessTokenService(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    /**
     * Returns a valid Google access token for the signed-in user, refreshing it
     * automatically when the current token has expired.
     */
    public String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new GoogleAuthenticationRequiredException("Sign in with Google to continue.");
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(oauthToken.getAuthorizedClientRegistrationId())
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient client;
        try {
            client = authorizedClientManager.authorize(authorizeRequest);
        } catch (ClientAuthorizationException ex) {
            throw new GoogleAuthenticationRequiredException(
                    "Google access expired. Please sign in again.", ex);
        }

        if (client == null || client.getAccessToken() == null
                || client.getAccessToken().getTokenValue() == null
                || client.getAccessToken().getTokenValue().isBlank()) {
            throw new GoogleAuthenticationRequiredException(
                    "Google access expired. Please sign in again.");
        }

        return client.getAccessToken().getTokenValue();
    }
}
