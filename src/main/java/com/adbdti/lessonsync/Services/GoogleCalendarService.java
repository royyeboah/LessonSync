package com.adbdti.lessonsync.Services;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {

    @Autowired
    private Calendar calendar;

    public String createTimeTable(String name) throws IOException {

        com.google.api.services.calendar.model.Calendar timeTable = new com.google.api.services.calendar.model.Calendar();
        timeTable.setSummary(name);
        timeTable.setTimeZone("UTC");

        com.google.api.services.calendar.model.Calendar createdCalendar = calendar.calendars().insert(timeTable).execute();
        return createdCalendar.getId();

    }

    public String createLecture(String name, String location, String lecturer_name
    , LocalDateTime start, LocalDateTime end, String lecturesEndDate) throws IOException {

        Event event = new Event();

        event.setSummary(name);
        event.setLocation(location);
        event.setDescription(lecturer_name);

        String rfc3339Start = start.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String rfc3339end = end.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        EventDateTime startTime = new EventDateTime()
                .setDateTime(new DateTime(rfc3339Start))
                .setTimeZone("UTC");

        EventDateTime endTime = new EventDateTime()
                .setDateTime(new DateTime(rfc3339end))
                .setTimeZone("UTC");

        event.setStart(startTime);
        event.setEnd(endTime);
        event.setRecurrence(List.of("RRULE:FREQ=WEEKLY;UNTIL=20251212T230000Z"));

        Event recurringEvent = calendar.events().insert("primary", event).execute();

        return recurringEvent.getHtmlLink();
    }




}
