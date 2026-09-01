package com.adbdti.lessonsync.Services;

import com.adbdti.lessonsync.Auth.GoogleAccessTokenService;
import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "LessonSync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleAccessTokenService googleAccessTokenService;
    private final TimeTableRepository timeTableRepository;
    private final NetHttpTransport httpTransport;

    @Autowired
    public GoogleCalendarService(GoogleAccessTokenService googleAccessTokenService,
                                 TimeTableRepository timeTableRepository)
            throws GeneralSecurityException, IOException {
        this.googleAccessTokenService = googleAccessTokenService;
        this.timeTableRepository = timeTableRepository;
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    GoogleCalendarService(GoogleAccessTokenService googleAccessTokenService,
                          TimeTableRepository timeTableRepository,
                          NetHttpTransport httpTransport) {
        this.googleAccessTokenService = googleAccessTokenService;
        this.timeTableRepository = timeTableRepository;
        this.httpTransport = httpTransport;
    }

    public String createTimeTable(String name, LocalDateTime startDate, LocalDateTime endDate) throws IOException {

        List<TimeTable> timeTableList = (List<TimeTable>) timeTableRepository.findAll();
        TimeTable timeTable = timeTableList.get(0);
        timeTable.setName(name);
        timeTable.setStartDate(startDate);
        timeTable.setEndDate(endDate);


        com.google.api.services.calendar.model.Calendar googleTimeTable = new com.google.api.services.calendar.model.Calendar();
        googleTimeTable.setSummary(name);
        googleTimeTable.setTimeZone("UTC");

        com.google.api.services.calendar.model.Calendar createdCalendar = calendar().calendars().insert(googleTimeTable).execute();
        timeTable.setGoogleCalendarId(createdCalendar.getId());

        timeTableRepository.save(timeTable);

        return createdCalendar.getId();

    }

    public String createLecture(String name, String location, String lecturer_name,
                                String start, String end, int reminderTime, String dayOfWeek) throws IOException {
    
        List<TimeTable> timeTableList = (List<TimeTable>) timeTableRepository.findAll();
        TimeTable timeTable = timeTableList.get(0);
    
        Event event = new Event();
    
        event.setSummary(name);
        event.setLocation(location);
        event.setDescription(lecturer_name);
    
        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(reminderTime)
        };
    
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);
    
        LocalTime justStartTime = LocalTime.parse(start);
        LocalTime justEndTime = LocalTime.parse(end);
    
        // Find the next occurrence of the specified day of the week after the timetable start date
        LocalDate startDate = timeTable.getStartDate().toLocalDate();
        LocalDate firstLectureDate = startDate
                .with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase())));
    
        // Combine the first lecture date with the parsed times
        LocalDateTime firstStartTime = firstLectureDate.atTime(justStartTime);
        LocalDateTime firstEndTime = firstLectureDate.atTime(justEndTime);
    
        // Format the LocalDateTime objects into RFC 3339 strings
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String rfc3339Start = firstStartTime.format(formatter);
        String rfc3339End = firstEndTime.format(formatter);
    
        EventDateTime startTime = new EventDateTime()
                .setDateTime(new DateTime(rfc3339Start))
                .setTimeZone("UTC");
    
        EventDateTime endTime = new EventDateTime()
                .setDateTime(new DateTime(rfc3339End))
                .setTimeZone("UTC");
    
        String lastDay = timeTable.getEndDate().format(formatter);
        lastDay = lastDay.replaceAll("-","")
                .replaceAll(":","");
    
        event.setStart(startTime);
        event.setEnd(endTime);
        event.setRecurrence(List.of("RRULE:FREQ=WEEKLY;UNTIL="+lastDay));
    
        Event recurringEvent = calendar().events().insert(timeTable.getGoogleCalendarId(), event).execute();
    
        return recurringEvent.getHtmlLink();
    }

    Calendar calendar() {
        String accessToken = googleAccessTokenService.getAccessToken();
        return new Calendar.Builder(httpTransport, JSON_FACTORY, request ->
                request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}
