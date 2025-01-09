package com.adbdti.lessonsync.Repository;

import com.adbdti.lessonsync.Model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.List;

@Repository
public interface LectureRepository extends CrudRepository<Lecture, String> {

    List<Lecture> findByDay(String day);
    List<Lecture> findByCourse(String course);
    List<Lecture> findByLecturerName(String lecturerName);
    List<Lecture> findByTimeTableId(String timeTableId);
}
