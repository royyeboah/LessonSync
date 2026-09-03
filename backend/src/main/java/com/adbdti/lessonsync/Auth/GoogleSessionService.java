package com.adbdti.lessonsync.Auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Remembers which Google account the current browser session is connected to.
 *
 * <p>Only the account id and email live in the session; the tokens themselves stay in the
 * credential store so that a returning user is reconnected without another consent screen.
 */
@Service
public class GoogleSessionService {

    private static final String USER_ID_ATTRIBUTE = "google.userId";
    private static final String EMAIL_ATTRIBUTE = "google.email";
    private static final String STATE_ATTRIBUTE = "google.oauthState";

    private final HttpSession session;

    public GoogleSessionService(HttpSession session) {
        this.session = session;
    }

    public String getUserId() {
        return (String) session.getAttribute(USER_ID_ATTRIBUTE);
    }

    public String getEmail() {
        return (String) session.getAttribute(EMAIL_ATTRIBUTE);
    }

    public void setAccount(GoogleAccount account) {
        session.setAttribute(USER_ID_ATTRIBUTE, account.userId());
        session.setAttribute(EMAIL_ATTRIBUTE, account.email());
    }

    public void clearAccount() {
        session.removeAttribute(USER_ID_ATTRIBUTE);
        session.removeAttribute(EMAIL_ATTRIBUTE);
    }

    public void storeState(String state) {
        session.setAttribute(STATE_ATTRIBUTE, state);
    }

    /**
     * Compares the state returned by Google with the one issued for this session and clears it, so
     * that a callback URL cannot be replayed.
     */
    public boolean consumeState(String state) {
        Object expected = session.getAttribute(STATE_ATTRIBUTE);
        session.removeAttribute(STATE_ATTRIBUTE);
        return state != null && state.equals(expected);
    }

    public String requireUserId() {
        String userId = getUserId();
        if (userId == null || userId.isBlank()) {
            throw new GoogleAuthRequiredException("Connect your Google account before using the calendar.");
        }
        return userId;
    }
}
