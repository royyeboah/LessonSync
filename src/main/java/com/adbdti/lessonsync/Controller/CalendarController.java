package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Model.Lecture;
import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.LectureRepository;
import com.adbdti.lessonsync.Services.CalendarService;
import com.adbdti.lessonsync.Services.GoogleCalendarService;
import com.adbdti.lessonsync.Services.VertexAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final VertexAIService vertexAIService;
    private final GoogleCalendarService googleCalendarService;
    private final LectureRepository lectureRepository;


    @Autowired
    public CalendarController(CalendarService calendarService, VertexAIService vertexAIService, GoogleCalendarService googleCalendarService, LectureRepository lectureRepository) {
        this.calendarService = calendarService;
        this.vertexAIService = vertexAIService;
        this.googleCalendarService = googleCalendarService;
        this.lectureRepository = lectureRepository;
    }

    @PostMapping("/lecture")
    public TimeTable timetable() {

        String jsonString = vertexAIService.generateContent();

        return calendarService.insertTimetable(jsonString);
    }


    @GetMapping("/createEvents")
    public ResponseEntity<List<String>> postAllLectures() throws IOException {
        List<Lecture> lectures = lectureRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<String> eventLinks = new ArrayList<>();
        
        for (Lecture lecture : lectures) {
            String eventLink = googleCalendarService.createLecture(
                lecture.getCourse(),
                lecture.getLocation(),
                lecture.getLecturer_name(),
                LocalDateTime.parse(lecture.getStart_time(), formatter),
                LocalDateTime.parse(lecture.getEnd_time(), formatter),
                lecture.getTime_table().getEndDate().toString()
            );
            eventLinks.add(eventLink);
        }
        
        return ResponseEntity.ok(eventLinks);
    }

    @GetMapping("/createCalendar")
    public ResponseEntity<String> createCalendar(@RequestParam String name) throws Exception{

        return ResponseEntity.ok(googleCalendarService.createTimeTable(name));
    }

}
