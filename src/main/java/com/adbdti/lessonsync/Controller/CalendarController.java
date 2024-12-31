package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Model.Lecture;
import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Services.CalendarService;
import com.adbdti.lessonsync.Services.VertexAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;

@RestController
public class CalendarController {

    private final CalendarService calendarService;
    private final VertexAIService vertexAIService;


    @Autowired
    public CalendarController(CalendarService calendarService, VertexAIService vertexAIService) {
        this.calendarService = calendarService;
        this.vertexAIService = vertexAIService;
    }

    @PostMapping("/timetable")
    public TimeTable timetable() {

        String jsonString = vertexAIService.generateContent();

        return calendarService.insertTimetable(jsonString);
    }
}
