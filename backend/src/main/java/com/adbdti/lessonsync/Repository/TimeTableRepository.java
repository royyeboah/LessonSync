package com.adbdti.lessonsync.Repository;

import com.adbdti.lessonsync.Model.TimeTable;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds the timetable a student is currently building, for the life of their session.
 *
 * @see LectureRepository for why this is not backed by a database
 */
@Repository
@SessionScope
public class TimeTableRepository {

    private final Map<String, TimeTable> timeTables = new LinkedHashMap<>();

    public synchronized TimeTable save(TimeTable timeTable) {
        if (timeTable.getId() == null || timeTable.getId().isBlank()) {
            timeTable.setId(UUID.randomUUID().toString());
        }
        timeTables.put(timeTable.getId(), timeTable);
        return timeTable;
    }

    public synchronized List<TimeTable> findAll() {
        return new ArrayList<>(timeTables.values());
    }

    public synchronized Optional<TimeTable> findById(String id) {
        return Optional.ofNullable(timeTables.get(id));
    }

    /**
     * The timetable the student is working on, empty until they have uploaded a schedule.
     */
    public synchronized Optional<TimeTable> findCurrent() {
        return timeTables.values().stream().findFirst();
    }

    public synchronized void deleteAll() {
        timeTables.clear();
    }
}
