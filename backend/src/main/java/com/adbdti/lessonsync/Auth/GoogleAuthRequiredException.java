package com.adbdti.lessonsync.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an endpoint needs Google Calendar access but the current
 * session has no (valid) Google credential. Mapped to HTTP 401 so the
 * frontend can redirect the user to the Google sign-in flow.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Google authorization required")
public class GoogleAuthRequiredException extends RuntimeException {

    public GoogleAuthRequiredException() {
        super("Google authorization required");
    }

    public GoogleAuthRequiredException(Throwable cause) {
        super("Google authorization required", cause);
    }
}
