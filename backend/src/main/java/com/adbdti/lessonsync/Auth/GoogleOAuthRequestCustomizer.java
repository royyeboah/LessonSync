package com.adbdti.lessonsync.Auth;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.function.Consumer;

/**
 * Asks Google for a refresh token so access tokens can be renewed without
 * pasting a new token into the app every hour.
 */
public final class GoogleOAuthRequestCustomizer {

    public static final Consumer<OAuth2AuthorizationRequest.Builder> OFFLINE_ACCESS = builder ->
            builder.additionalParameters(params -> {
                params.put("access_type", "offline");
                params.put("prompt", "consent");
            });

    private GoogleOAuthRequestCustomizer() {
    }
}
