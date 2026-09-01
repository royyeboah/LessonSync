package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Auth.GoogleAccount;
import com.adbdti.lessonsync.Auth.GoogleAuthService;
import com.adbdti.lessonsync.Auth.GoogleSessionService;
import com.adbdti.lessonsync.Config.GoogleOAuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoints that drive the Google sign in flow from the browser.
 *
 * <p>{@code /auth/google/login} is meant to be opened as a top level navigation, not fetched with
 * XHR, so that the session cookie is established before Google redirects back to
 * {@code /auth/google/callback}.
 */
@RestController
@RequestMapping("/auth/google")
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    private final GoogleAuthService googleAuthService;
    private final GoogleSessionService googleSessionService;
    private final GoogleOAuthProperties properties;

    public GoogleAuthController(GoogleAuthService googleAuthService,
                                GoogleSessionService googleSessionService,
                                GoogleOAuthProperties properties) {
        this.googleAuthService = googleAuthService;
        this.googleSessionService = googleSessionService;
        this.properties = properties;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean connected = googleAuthService.hasStoredCredential(googleSessionService.getUserId());

        if (!connected) {
            googleSessionService.clearAccount();
        }

        body.put("configured", googleAuthService.isConfigured());
        body.put("connected", connected);
        body.put("email", connected ? googleSessionService.getEmail() : null);
        return ResponseEntity.ok(body);
    }

    /**
     * Redirects the browser to Google's consent screen.
     */
    @GetMapping("/login")
    public RedirectView login() {
        String state = googleAuthService.newState();
        googleSessionService.storeState(state);
        return new RedirectView(googleAuthService.buildAuthorizationUrl(state));
    }

    @GetMapping("/callback")
    public RedirectView callback(@RequestParam(required = false) String code,
                                 @RequestParam(required = false) String state,
                                 @RequestParam(required = false) String error) {

        if (error != null) {
            log.info("Google authorization was not granted: {}", error);
            return redirectToFrontend(properties.getFailureRedirectUri(), "google_error", error);
        }

        if (!googleSessionService.consumeState(state)) {
            log.warn("Rejected a Google callback whose state did not match the session");
            return redirectToFrontend(properties.getFailureRedirectUri(), "google_error", "invalid_state");
        }

        if (code == null || code.isBlank()) {
            return redirectToFrontend(properties.getFailureRedirectUri(), "google_error", "missing_code");
        }

        try {
            GoogleAccount account = googleAuthService.exchangeCode(code);
            googleSessionService.setAccount(account);
            return redirectToFrontend(properties.getSuccessRedirectUri(), "google", "connected");
        } catch (IOException e) {
            log.error("Failed to exchange the Google authorization code", e);
            return redirectToFrontend(properties.getFailureRedirectUri(), "google_error", "token_exchange_failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        googleAuthService.disconnect(googleSessionService.getUserId());
        googleSessionService.clearAccount();
        return ResponseEntity.noContent().build();
    }

    /**
     * Exposes the consent URL for callers that would rather navigate themselves than follow a 302.
     */
    @GetMapping("/authorization-url")
    public ResponseEntity<Map<String, String>> authorizationUrl() {
        if (!googleAuthService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        String state = googleAuthService.newState();
        googleSessionService.storeState(state);
        return ResponseEntity.ok(Map.of("authorizationUrl", googleAuthService.buildAuthorizationUrl(state)));
    }

    private RedirectView redirectToFrontend(String target, String param, String value) {
        String url = UriComponentsBuilder.fromUriString(target)
                .queryParam(param, value)
                .build()
                .toUriString();
        return new RedirectView(url);
    }
}
