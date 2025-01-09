package com.adbdti.lessonsync.Repository;


import com.adbdti.lessonsync.Model.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeTableRepository extends CrudRepository<TimeTable, String> {
}
