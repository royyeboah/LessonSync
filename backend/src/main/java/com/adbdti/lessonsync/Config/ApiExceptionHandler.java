package com.adbdti.lessonsync.Config;

import com.adbdti.lessonsync.Auth.GoogleAuthRequiredException;
import com.adbdti.lessonsync.Auth.GoogleOAuthNotConfiguredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Turns authorization problems into responses the frontend can act on, rather than opaque 500s.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Signals the frontend that it should send the user through the Google sign in flow.
     */
    @ExceptionHandler(GoogleAuthRequiredException.class)
    public ResponseEntity<Map<String, String>> handleAuthRequired(GoogleAuthRequiredException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "google_auth_required", "message", e.getMessage()));
    }

    @ExceptionHandler(GoogleOAuthNotConfiguredException.class)
    public ResponseEntity<Map<String, String>> handleNotConfigured(GoogleOAuthNotConfiguredException e) {
        log.error("Google OAuth is not configured", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "google_oauth_not_configured", "message", e.getMessage()));
    }
}
