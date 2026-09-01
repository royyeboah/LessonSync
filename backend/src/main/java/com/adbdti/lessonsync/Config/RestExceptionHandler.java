package com.adbdti.lessonsync.Config;

import com.adbdti.lessonsync.Auth.GoogleAuthenticationRequiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(GoogleAuthenticationRequiredException.class)
    public ResponseEntity<Map<String, String>> handleGoogleAuthRequired(
            GoogleAuthenticationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }
}
