package com.adbdti.lessonsync.Model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Event {

    private String day;
    private String course;
    private String location;
    private String lecturer_name;
    private String group;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
}
