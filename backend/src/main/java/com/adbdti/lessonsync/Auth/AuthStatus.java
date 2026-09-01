package com.adbdti.lessonsync.Auth;

public record AuthStatus(boolean authenticated, String name, String email, String picture) {

    public static AuthStatus anonymous() {
        return new AuthStatus(false, null, null, null);
    }

    public static AuthStatus of(String name, String email, String picture) {
        return new AuthStatus(true, name, email, picture);
    }
}
