package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Auth.AuthStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthControllerTest {

    private final AuthController controller = new AuthController("http://localhost:4200");

    @Test
    void statusReturnsAnonymousWhenUserIsMissing() {
        AuthStatus status = controller.status(null);

        assertFalse(status.authenticated());
        assertNull(status.email());
        assertNull(status.name());
        assertNull(status.picture());
    }

    @Test
    void statusReturnsGoogleProfileWhenSignedIn() {
        OAuth2User user = new DefaultOAuth2User(
                List.of(),
                Map.of(
                        "sub", "abc",
                        "name", "Ada Lovelace",
                        "email", "ada@example.com",
                        "picture", "https://example.com/ada.png"
                ),
                "sub");

        AuthStatus status = controller.status(user);

        assertTrue(status.authenticated());
        assertEquals("Ada Lovelace", status.name());
        assertEquals("ada@example.com", status.email());
        assertEquals("https://example.com/ada.png", status.picture());
    }

    @Test
    void safeFrontendRedirectAllowsAppRelativePaths() {
        assertEquals("http://localhost:4200/upload", controller.safeFrontendRedirect("/upload"));
        assertEquals("http://localhost:4200/final", controller.safeFrontendRedirect("/final"));
    }

    @Test
    void safeFrontendRedirectRejectsOpenRedirects() {
        assertEquals("http://localhost:4200/", controller.safeFrontendRedirect(null));
        assertEquals("http://localhost:4200/", controller.safeFrontendRedirect("https://evil.example"));
        assertEquals("http://localhost:4200/", controller.safeFrontendRedirect("//evil.example"));
        assertEquals("http://localhost:4200/", controller.safeFrontendRedirect("upload"));
    }
}
