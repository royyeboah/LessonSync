package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Model.Lecture;
import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.LectureRepository;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import com.adbdti.lessonsync.Services.CalendarService;
import com.adbdti.lessonsync.Services.GoogleCalendarService;
import com.adbdti.lessonsync.Services.VertexAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final VertexAIService vertexAIService;
    private final GoogleCalendarService googleCalendarService;
    private final LectureRepository lectureRepository;
    private final TimeTableRepository timeTableRepository;


    @Autowired
    public CalendarController(CalendarService calendarService, VertexAIService vertexAIService, GoogleCalendarService googleCalendarService, LectureRepository lectureRepository, TimeTableRepository timeTableRepository) {
        this.calendarService = calendarService;
        this.vertexAIService = vertexAIService;
        this.googleCalendarService = googleCalendarService;
        this.lectureRepository = lectureRepository;
        this.timeTableRepository = timeTableRepository;
    }

    @PostMapping("/lecture")
    public TimeTable timetable() {

        String jsonString = vertexAIService.generateContent();

        return calendarService.insertTimetable(jsonString);
    }


    @GetMapping("/createEvents")
    public ResponseEntity<List<String>> postAllLectures() throws IOException {
        List<Lecture> lectures = (List<Lecture>) lectureRepository.findAll();
        List<String> eventLinks = new ArrayList<>();
        
        for (Lecture lecture : lectures) {
            String eventLink = googleCalendarService.createLecture(
                lecture.getCourse(),
                lecture.getLocation(),
                lecture.getLecturerName(),
                lecture.getStart_time(),
                lecture.getEnd_time()
            );
            eventLinks.add(eventLink);
        }

        timeTableRepository.deleteAll();
        
        return ResponseEntity.ok(eventLinks);
    }

    @PostMapping("/createCalendar")
    public ResponseEntity<String> createCalendar(@RequestParam String name,@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) throws Exception{

        return ResponseEntity.ok(googleCalendarService.createTimeTable(name,startDate, endDate));
    }


}
