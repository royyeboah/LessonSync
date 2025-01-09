package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Model.TimeTable;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeTableController {

    @Autowired
    private TimeTableRepository timeTableRepository;

    @GetMapping("timetables")
    public ResponseEntity<Iterable<TimeTable>> getAllTimeTables(){

        return ResponseEntity.ok(timeTableRepository.findAll());
    }
}
