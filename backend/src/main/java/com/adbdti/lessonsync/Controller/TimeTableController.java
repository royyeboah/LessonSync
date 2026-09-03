package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimeTableController {

    @Autowired
    private TimeTableRepository timeTableRepository;

    @GetMapping("timetables")
    public ResponseEntity<List<TimeTable>> getAllTimeTables(){

        return ResponseEntity.ok(timeTableRepository.findAll());
    }

    @GetMapping("timetable")
    public ResponseEntity<TimeTable> getCurrentTimeTable(){
        return timeTableRepository.findCurrent()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
