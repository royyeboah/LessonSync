package com.adbdti.lessonsync.Services;

import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoogleCalendarService {

    @Autowired
    private Calendar calendar;

    @Autowired
    private TimeTableRepository timeTableRepository;

    public String createTimeTable(String name, LocalDateTime startDate, LocalDateTime endDate) throws IOException {

        List<TimeTable> timeTableList = (List<TimeTable>) timeTableRepository.findAll();
        TimeTable timeTable = timeTableList.get(0);
        timeTable.setName(name);
        timeTable.setStartDate(startDate);
        timeTable.setEndDate(endDate);


        com.google.api.services.calendar.model.Calendar googleTimeTable = new com.google.api.services.calendar.model.Calendar();
        googleTimeTable.setSummary(name);
        googleTimeTable.setTimeZone("UTC");

        com.google.api.services.calendar.model.Calendar createdCalendar = calendar.calendars().insert(googleTimeTable).execute();
        timeTable.setGoogleCalendarId(createdCalendar.getId());

        timeTableRepository.save(timeTable);

        return createdCalendar.getId();

    }

    public String createLecture(String name, String location, String lecturer_name
    , String start, String end, int reminderTime) throws IOException {

        List<TimeTable> timeTableList = (List<TimeTable>) timeTableRepository.findAll();
        TimeTable timeTable = timeTableList.get(0);

        Event event = new Event();

        event.setSummary(name);
        event.setLocation(location);
        event.setDescription(lecturer_name);

        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("popup").setMinutes(reminderTime)
        };


        LocalTime justStartTime = LocalTime.parse(start);
        LocalTime justEndTime = LocalTime.parse(end);

        // Combine the timetable's start date with the parsed times
        LocalDateTime firstStartTime = timeTable.getStartDate().toLocalDate().atTime(justStartTime);
        LocalDateTime firstEndTime = timeTable.getStartDate().toLocalDate().atTime(justEndTime);

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

        String correctFormat = timeTable.getEndDate().format(formatter);
        correctFormat = correctFormat.replaceAll("-","")
                .replaceAll(":","");

        event.setStart(startTime);
        event.setEnd(endTime);
        event.setRecurrence(List.of("RRULE:FREQ=WEEKLY;UNTIL="+correctFormat));

        Event recurringEvent = calendar.events().insert(timeTable.getGoogleCalendarId(), event).execute();

        return recurringEvent.getHtmlLink();
    }




}
