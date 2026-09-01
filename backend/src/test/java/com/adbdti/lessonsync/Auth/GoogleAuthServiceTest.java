package com.adbdti.lessonsync.Auth;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleAuthServiceTest {

    @Mock
    private GoogleOAuthFlowProvider flowProvider;

    @Mock
    private GoogleAuthorizationCodeFlow flow;

    private GoogleAuthService service;

    @BeforeEach
    void setUp() {
        given(flowProvider.getFlow()).willReturn(flow);
        given(flowProvider.isConfigured()).willReturn(true);
        service = new GoogleAuthService(flowProvider);
    }

    @Test
    void issuesADifferentStateEachTime() {
        assertThat(service.newState()).isNotEqualTo(service.newState());
    }

    @Test
    void loadCredentialRejectsAnAbsentUser() {
        assertThatThrownBy(() -> service.loadCredential(null))
                .isInstanceOf(GoogleAuthRequiredException.class);
        assertThatThrownBy(() -> service.loadCredential("  "))
                .isInstanceOf(GoogleAuthRequiredException.class);
    }

    @Test
    void loadCredentialFailsWhenNothingIsStored() throws IOException {
        given(flow.loadCredential("user-1")).willReturn(null);

        assertThatThrownBy(() -> service.loadCredential("user-1"))
                .isInstanceOf(GoogleAuthRequiredException.class)
                .hasMessageContaining("No Google credential");
    }

    @Test
    void loadCredentialFailsWhenTheStoreCannotBeRead() throws IOException {
        given(flow.loadCredential("user-1")).willThrow(new IOException("redis is down"));

        assertThatThrownBy(() -> service.loadCredential("user-1"))
                .isInstanceOf(GoogleAuthRequiredException.class)
                .hasMessageContaining("redis is down");
    }

    @Test
    void loadCredentialReturnsAValidAccessTokenUntouched() throws IOException {
        Credential credential = newCredential();
        credential.setAccessToken("access-token");
        credential.setExpiresInSeconds(3600L);
        given(flow.loadCredential("user-1")).willReturn(credential);

        assertThat(service.loadCredential("user-1").getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void loadCredentialFailsWhenAnExpiredTokenHasNoRefreshToken() throws IOException {
        Credential credential = newCredential();
        credential.setAccessToken("expired-token");
        credential.setExpiresInSeconds(5L);
        given(flow.loadCredential("user-1")).willReturn(credential);

        assertThatThrownBy(() -> service.loadCredential("user-1"))
                .isInstanceOf(GoogleAuthRequiredException.class)
                .hasMessageContaining("no refresh token");
    }

    @Test
    void hasStoredCredentialIsFalseForAnUnknownUser() throws IOException {
        given(flow.loadCredential("user-1")).willReturn(null);

        assertThat(service.hasStoredCredential("user-1")).isFalse();
        assertThat(service.hasStoredCredential(null)).isFalse();
    }

    @Test
    void hasStoredCredentialIsTrueOnceARefreshTokenIsHeld() throws IOException {
        Credential credential = newCredential();
        credential.setRefreshToken("refresh-token");
        given(flow.loadCredential("user-1")).willReturn(credential);

        assertThat(service.hasStoredCredential("user-1")).isTrue();
    }

    @Test
    void hasStoredCredentialIsFalseWhenOAuthIsNotConfigured() {
        given(flowProvider.isConfigured()).willReturn(false);

        assertThat(service.hasStoredCredential("user-1")).isFalse();
    }

    private Credential newCredential() {
        return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
                .setTransport(new MockHttpTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                // Setting a refresh token requires somewhere to redeem it.
                .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
                .setClientAuthentication(
                        new ClientParametersAuthentication("test-client-id", "test-client-secret"))
                .build();
    }
}
