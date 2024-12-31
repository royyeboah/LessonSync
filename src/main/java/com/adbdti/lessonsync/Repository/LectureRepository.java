package com.adbdti.lessonsync.Repository;

import com.adbdti.lessonsync.Model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Calendar;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Integer> {
}
