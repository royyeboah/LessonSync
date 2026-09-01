package com.adbdti.lessonsync.Auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleAccessTokenServiceTest {

    @Mock
    private OAuth2AuthorizedClientManager authorizedClientManager;

    private GoogleAccessTokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new GoogleAccessTokenService(authorizedClientManager);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAccessTokenForSignedInGoogleUser() {
        SecurityContextHolder.getContext().setAuthentication(googleAuthentication());
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(authorizedClient("fresh-access-token"));

        assertEquals("fresh-access-token", tokenService.getAccessToken());
    }

    @Test
    void throwsWhenUserIsNotSignedInWithGoogle() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student", "password"));

        GoogleAuthenticationRequiredException ex = assertThrows(
                GoogleAuthenticationRequiredException.class,
                tokenService::getAccessToken);
        assertEquals("Sign in with Google to continue.", ex.getMessage());
    }

    @Test
    void throwsWhenThereIsNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertThrows(GoogleAuthenticationRequiredException.class, tokenService::getAccessToken);
    }

    @Test
    void throwsWhenGoogleRefreshFails() {
        SecurityContextHolder.getContext().setAuthentication(googleAuthentication());
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenThrow(new ClientAuthorizationException(
                        new OAuth2Error("invalid_grant"), "google"));

        GoogleAuthenticationRequiredException ex = assertThrows(
                GoogleAuthenticationRequiredException.class,
                tokenService::getAccessToken);
        assertEquals("Google access expired. Please sign in again.", ex.getMessage());
    }

    @Test
    void throwsWhenAuthorizedClientHasNoToken() {
        SecurityContextHolder.getContext().setAuthentication(googleAuthentication());
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class)))
                .thenReturn(null);

        assertThrows(GoogleAuthenticationRequiredException.class, tokenService::getAccessToken);
    }

    private static OAuth2AuthenticationToken googleAuthentication() {
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(),
                Map.of("sub", "google-user-1", "email", "student@example.com", "name", "Student"),
                "sub");
        return new OAuth2AuthenticationToken(principal, List.of(), "google");
    }

    private static OAuth2AuthorizedClient authorizedClient(String tokenValue) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .scope("openid", "https://www.googleapis.com/auth/calendar")
                .build();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                tokenValue,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Set.of("https://www.googleapis.com/auth/calendar"));

        return new OAuth2AuthorizedClient(registration, "google-user-1", accessToken);
    }
}
