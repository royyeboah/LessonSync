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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public TimeTable timetable(@RequestParam("file")MultipartFile file) {

        String jsonString = vertexAIService.generateContent(file);

        return calendarService.insertTimetable(jsonString);
    }


    @GetMapping("/createEvents")
    public ResponseEntity<List<String>> postAllLectures(@RequestParam Integer reminderTime) throws IOException {
        List<Lecture> lectures = (List<Lecture>) lectureRepository.findAll();
        List<String> eventLinks = new ArrayList<>();
        
        for (Lecture lecture : lectures) {
            String eventLink = googleCalendarService.createLecture(
                lecture.getCourse(),
                lecture.getLocation(),
                lecture.getLecturerName(),
                lecture.getStart_time(),
                lecture.getEnd_time(),
                reminderTime,
                lecture.getDay()
            );
            eventLinks.add(eventLink);
        }

        lectureRepository.deleteAll();
        timeTableRepository.deleteAll();

        
        return ResponseEntity.ok(eventLinks);
    }

    @PostMapping("/createCalendar")
    public ResponseEntity<String> createCalendar(@RequestBody TimeTable timetable) throws Exception{

        return ResponseEntity.ok(googleCalendarService.createTimeTable(timetable.getName(),timetable.getStartDate(), timetable.getEndDate()));
    }

    @GetMapping("/lectures")
    public List<Lecture> getAllLectures(){
        return (List<Lecture>) lectureRepository.findAll();
    }

    @PutMapping("/lecture/{id}")
    public ResponseEntity<Lecture> editLecture(@PathVariable String id, @RequestBody Lecture updatedLecture) {
        Optional<Lecture> lectureOptional = lectureRepository.findById(id);
        
        if (lectureOptional.isPresent()) {
            Lecture lecture = lectureOptional.get();
            
            if (updatedLecture.getDay() != null) lecture.setDay(updatedLecture.getDay());
            if (updatedLecture.getCourse() != null) lecture.setCourse(updatedLecture.getCourse());
            if (updatedLecture.getLocation() != null) lecture.setLocation(updatedLecture.getLocation());
            if (updatedLecture.getLecturerName() != null) lecture.setLecturerName(updatedLecture.getLecturerName());
            if (updatedLecture.getGroupName() != null) lecture.setGroupName(updatedLecture.getGroupName());
            if (updatedLecture.getStart_time() != null) lecture.setStart_time(updatedLecture.getStart_time());
            if (updatedLecture.getEnd_time() != null) lecture.setEnd_time(updatedLecture.getEnd_time());
            if (updatedLecture.getTimeTableId() != null) lecture.setTimeTableId(updatedLecture.getTimeTableId());
            
            Lecture savedLecture = lectureRepository.save(lecture);
            return ResponseEntity.ok(savedLecture);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
