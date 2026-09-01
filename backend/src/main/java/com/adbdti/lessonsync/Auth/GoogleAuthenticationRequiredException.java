package com.adbdti.lessonsync.Auth;

public class GoogleAuthenticationRequiredException extends RuntimeException {

    public GoogleAuthenticationRequiredException(String message) {
        super(message);
    }

    public GoogleAuthenticationRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
