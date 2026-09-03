package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Auth.GoogleAccount;
import com.adbdti.lessonsync.Auth.GoogleAuthService;
import com.adbdti.lessonsync.Auth.GoogleSessionService;
import com.adbdti.lessonsync.Config.GoogleOAuthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoogleAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoogleAuthControllerTest {

    private static final String SUCCESS_URI = "http://localhost:4200/";
    private static final String FAILURE_URI = "http://localhost:4200/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleAuthService googleAuthService;

    @MockBean
    private GoogleSessionService googleSessionService;

    @TestConfiguration
    static class Properties {
        @Bean
        GoogleOAuthProperties googleOAuthProperties() {
            GoogleOAuthProperties properties = new GoogleOAuthProperties();
            properties.setSuccessRedirectUri(SUCCESS_URI);
            properties.setFailureRedirectUri(FAILURE_URI);
            return properties;
        }
    }

    @Test
    void statusReportsADisconnectedSession() throws Exception {
        given(googleAuthService.isConfigured()).willReturn(true);
        given(googleAuthService.hasStoredCredential(null)).willReturn(false);

        mockMvc.perform(get("/auth/google/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(true))
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void statusReportsAConnectedSession() throws Exception {
        given(googleAuthService.isConfigured()).willReturn(true);
        given(googleSessionService.getUserId()).willReturn("user-1");
        given(googleSessionService.getEmail()).willReturn("student@example.com");
        given(googleAuthService.hasStoredCredential("user-1")).willReturn(true);

        mockMvc.perform(get("/auth/google/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.email").value("student@example.com"));
    }

    @Test
    void loginRedirectsToGoogleAndRemembersTheState() throws Exception {
        given(googleAuthService.newState()).willReturn("state-1");
        given(googleAuthService.buildAuthorizationUrl("state-1"))
                .willReturn("https://accounts.google.com/o/oauth2/auth?state=state-1");

        mockMvc.perform(get("/auth/google/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("https://accounts.google.com/o/oauth2/auth?state=state-1"));

        verify(googleSessionService).storeState("state-1");
    }

    @Test
    void callbackStoresTheAccountOnSuccess() throws Exception {
        given(googleSessionService.consumeState("state-1")).willReturn(true);
        given(googleAuthService.exchangeCode("code-1"))
                .willReturn(new GoogleAccount("user-1", "student@example.com"));

        mockMvc.perform(get("/auth/google/callback").param("code", "code-1").param("state", "state-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SUCCESS_URI + "?google=connected"));

        verify(googleSessionService).setAccount(new GoogleAccount("user-1", "student@example.com"));
    }

    @Test
    void callbackRejectsAMismatchedState() throws Exception {
        given(googleSessionService.consumeState("forged")).willReturn(false);

        mockMvc.perform(get("/auth/google/callback").param("code", "code-1").param("state", "forged"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FAILURE_URI + "?google_error=invalid_state"));

        verify(googleAuthService, never()).exchangeCode(anyString());
        verify(googleSessionService, never()).setAccount(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void callbackForwardsAConsentDenial() throws Exception {
        mockMvc.perform(get("/auth/google/callback").param("error", "access_denied"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FAILURE_URI + "?google_error=access_denied"));

        verify(googleAuthService, never()).exchangeCode(anyString());
    }

    @Test
    void callbackReportsAFailedTokenExchange() throws Exception {
        given(googleSessionService.consumeState("state-1")).willReturn(true);
        given(googleAuthService.exchangeCode("code-1")).willThrow(new IOException("boom"));

        mockMvc.perform(get("/auth/google/callback").param("code", "code-1").param("state", "state-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(FAILURE_URI + "?google_error=token_exchange_failed"));
    }

    @Test
    void logoutRevokesTheStoredCredential() throws Exception {
        given(googleSessionService.getUserId()).willReturn("user-1");

        mockMvc.perform(post("/auth/google/logout"))
                .andExpect(status().isNoContent());

        verify(googleAuthService).disconnect("user-1");
        verify(googleSessionService).clearAccount();
    }

    @Test
    void authorizationUrlIsUnavailableWhenOAuthIsNotConfigured() throws Exception {
        given(googleAuthService.isConfigured()).willReturn(false);

        mockMvc.perform(get("/auth/google/authorization-url"))
                .andExpect(status().isServiceUnavailable());
    }
}
