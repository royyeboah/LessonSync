package com.adbdti.lessonsync.Model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TimeTable {

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
