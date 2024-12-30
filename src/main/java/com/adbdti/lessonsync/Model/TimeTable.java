package com.adbdti.lessonsync.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "time_table", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lecture> lectures;
}
