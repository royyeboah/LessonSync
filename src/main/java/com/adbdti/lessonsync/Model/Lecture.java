package com.adbdti.lessonsync.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String day;
    private String course;
    private String location;
    private String lecturer_name;
    private String groupName;
    private LocalDateTime start_time;
    private LocalDateTime end_time;

    @ManyToOne
    @JoinColumn(name="time_table_id", nullable=false)
    private TimeTable time_table;
}
