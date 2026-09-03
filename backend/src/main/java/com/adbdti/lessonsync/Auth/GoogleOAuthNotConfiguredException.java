package com.adbdti.lessonsync.Auth;

/**
 * Raised when the application itself is missing its Google OAuth client credentials, which is a
 * deployment problem rather than something the end user can fix by signing in again.
 */
public class GoogleOAuthNotConfiguredException extends RuntimeException {

    public GoogleOAuthNotConfiguredException(String message) {
        super(message);
    }

    public GoogleOAuthNotConfiguredException(String message, Throwable cause) {
        super(message, cause);
    }
}
