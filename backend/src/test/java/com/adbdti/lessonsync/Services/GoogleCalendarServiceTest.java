package com.adbdti.lessonsync.Services;

import com.adbdti.lessonsync.Auth.GoogleAccessTokenService;
import com.adbdti.lessonsync.Auth.GoogleAuthenticationRequiredException;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleCalendarServiceTest {

    @Mock
    private GoogleAccessTokenService googleAccessTokenService;

    @Mock
    private TimeTableRepository timeTableRepository;

    private GoogleCalendarService googleCalendarService;

    @BeforeEach
    void setUp() {
        googleCalendarService = new GoogleCalendarService(
                googleAccessTokenService,
                timeTableRepository,
                new NetHttpTransport());
    }

    @Test
    void calendarClientUsesTheSignedInUsersAccessToken() throws Exception {
        when(googleAccessTokenService.getAccessToken()).thenReturn("ya29.live-token");

        Calendar calendar = googleCalendarService.calendar();
        HttpRequest request = calendar.getRequestFactory()
                .buildGetRequest(new GenericUrl("https://www.googleapis.com/calendar/v3/users/me/calendarList"));

        assertEquals("Bearer ya29.live-token", request.getHeaders().getAuthorization());
        verify(googleAccessTokenService).getAccessToken();
    }

    @Test
    void calendarClientDoesNotFallBackToAHardcodedToken() {
        when(googleAccessTokenService.getAccessToken())
                .thenThrow(new GoogleAuthenticationRequiredException("Sign in with Google to continue."));

        GoogleAuthenticationRequiredException ex = assertThrows(
                GoogleAuthenticationRequiredException.class,
                googleCalendarService::calendar);

        assertEquals("Sign in with Google to continue.", ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("sign in"));
    }
}
