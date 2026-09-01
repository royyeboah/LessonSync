package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Auth.GoogleAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String STATE_SESSION_KEY = "google_oauth_state";
    private static final String RETURN_TO_SESSION_KEY = "google_oauth_return_to";
    private static final String DEFAULT_RETURN_TO = "/upload";

    private final GoogleAuthService googleAuthService;
    private final String frontendUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(GoogleAuthService googleAuthService,
                          @Value("${app.frontend-url}") String frontendUrl) {
        this.googleAuthService = googleAuthService;
        this.frontendUrl = frontendUrl;
    }

    /**
     * Starts the OAuth flow: remembers where to send the user afterwards and
     * redirects the browser to Google's consent screen.
     */
    @GetMapping("/google/login")
    public void login(@RequestParam(defaultValue = DEFAULT_RETURN_TO) String returnTo,
                      HttpSession session,
                      HttpServletResponse response) throws IOException {
        String state = new BigInteger(130, secureRandom).toString(32);
        session.setAttribute(STATE_SESSION_KEY, state);
        session.setAttribute(RETURN_TO_SESSION_KEY, sanitizeReturnTo(returnTo));
        response.sendRedirect(googleAuthService.buildAuthorizationUrl(state));
    }

    /**
     * Google redirects here after consent. Exchanges the authorization code
     * for tokens, stores them for this session and sends the user back to the
     * frontend.
     */
    @GetMapping("/google/callback")
    public void callback(@RequestParam(required = false) String code,
                         @RequestParam(required = false) String state,
                         @RequestParam(required = false) String error,
                         HttpSession session,
                         HttpServletResponse response) throws IOException {
        String expectedState = (String) session.getAttribute(STATE_SESSION_KEY);
        String returnTo = (String) session.getAttribute(RETURN_TO_SESSION_KEY);
        session.removeAttribute(STATE_SESSION_KEY);
        session.removeAttribute(RETURN_TO_SESSION_KEY);
        if (returnTo == null) {
            returnTo = DEFAULT_RETURN_TO;
        }

        if (error != null || code == null || expectedState == null || !expectedState.equals(state)) {
            String reason = error != null ? error : "invalid_oauth_response";
            response.sendRedirect(frontendUrl + returnTo + "?authError="
                    + URLEncoder.encode(reason, StandardCharsets.UTF_8));
            return;
        }

        googleAuthService.exchangeAndStoreCode(code, session.getId());
        response.sendRedirect(frontendUrl + returnTo);
    }

    @GetMapping("/status")
    public Map<String, Boolean> status(HttpSession session) {
        return Map.of("authenticated", googleAuthService.isAuthorized(session.getId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) throws IOException {
        googleAuthService.revoke(session.getId());
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    /** Only allow relative paths so the callback cannot be abused as an open redirect. */
    private String sanitizeReturnTo(String returnTo) {
        if (returnTo != null && returnTo.startsWith("/") && !returnTo.startsWith("//")) {
            return returnTo;
        }
        return DEFAULT_RETURN_TO;
    }
}
