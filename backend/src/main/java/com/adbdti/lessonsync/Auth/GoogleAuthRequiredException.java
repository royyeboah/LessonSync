package com.adbdti.lessonsync.Auth;

/**
 * Raised when a request needs to act on the caller's Google Calendar but no usable credential is
 * stored for the current session. The caller is expected to run the authorization flow again.
 */
public class GoogleAuthRequiredException extends RuntimeException {

    public GoogleAuthRequiredException(String message) {
        super(message);
    }
}
