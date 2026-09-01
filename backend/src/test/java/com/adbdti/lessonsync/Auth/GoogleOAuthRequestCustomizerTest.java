package com.adbdti.lessonsync.Auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GoogleOAuthRequestCustomizerTest {

    @Test
    void requestsOfflineAccessAndConsentSoGoogleReturnsARefreshToken() {
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client")
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .scope("openid", "https://www.googleapis.com/auth/calendar")
                .state("state");

        GoogleOAuthRequestCustomizer.OFFLINE_ACCESS.accept(builder);
        OAuth2AuthorizationRequest customized = builder.build();

        assertEquals("offline", customized.getAdditionalParameters().get("access_type"));
        assertEquals("consent", customized.getAdditionalParameters().get("prompt"));
    }

    @Test
    void customizerIsNullSafeWhenBuilderHasNoExistingExtraParameters() {
        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client")
                .redirectUri("http://localhost:8080/login/oauth2/code/google");

        GoogleOAuthRequestCustomizer.OFFLINE_ACCESS.accept(builder);

        OAuth2AuthorizationRequest customized = builder.state("state").build();
        assertEquals("offline", customized.getAdditionalParameters().get("access_type"));
        assertNull(customized.getAdditionalParameters().get("missing"));
    }
}
