package com.adbdti.lessonsync.Services;

import com.adbdti.lessonsync.Auth.GoogleAuthService;
import com.adbdti.lessonsync.Auth.GoogleOAuthFlowProvider;
import com.adbdti.lessonsync.Auth.GoogleSessionService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.Calendar;
import org.springframework.stereotype.Component;

/**
 * Builds a Calendar client bound to the Google account of the current session.
 *
 * <p>This replaces the old application wide {@code Calendar} bean, which meant every student's
 * timetable ended up on whichever account happened to have authorized the server.
 */
@Component
public class GoogleCalendarClientFactory {

    private static final String APPLICATION_NAME = "LessonSync";

    private final GoogleAuthService googleAuthService;
    private final GoogleSessionService googleSessionService;
    private final GoogleOAuthFlowProvider flowProvider;

    public GoogleCalendarClientFactory(GoogleAuthService googleAuthService,
                                       GoogleSessionService googleSessionService,
                                       GoogleOAuthFlowProvider flowProvider) {
        this.googleAuthService = googleAuthService;
        this.googleSessionService = googleSessionService;
        this.flowProvider = flowProvider;
    }

    /**
     * @throws com.adbdti.lessonsync.Auth.GoogleAuthRequiredException if the session has no usable
     *                                                               Google credential
     */
    public Calendar forCurrentUser() {
        Credential credential = googleAuthService.loadCredential(googleSessionService.requireUserId());

        return new Calendar.Builder(
                flowProvider.getHttpTransport(), flowProvider.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
