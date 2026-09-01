package com.adbdti.lessonsync.Config;

import com.adbdti.lessonsync.Auth.GoogleAuthenticationRequiredException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void missingGoogleLoginReturnsUnauthorized() {
        ResponseEntity<Map<String, String>> response = handler.handleGoogleAuthRequired(
                new GoogleAuthenticationRequiredException("Sign in with Google to continue."));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Sign in with Google to continue.", response.getBody().get("error"));
    }
}
