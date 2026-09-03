package com.adbdti.lessonsync.Repository;

import com.adbdti.lessonsync.Model.Lecture;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds the lectures read out of one student's uploaded schedule while they correct them.
 *
 * <p>The data only has to live from the upload until the events are created, so it is kept in the
 * HTTP session rather than in a database. Scoping it to the session also keeps one student's
 * classes from being written to another student's calendar, and it is discarded automatically when
 * the session ends.
 */
@Repository
@SessionScope
public class LectureRepository {

    private final Map<String, Lecture> lectures = new LinkedHashMap<>();

    public synchronized Lecture save(Lecture lecture) {
        if (lecture.getId() == null || lecture.getId().isBlank()) {
            lecture.setId(UUID.randomUUID().toString());
        }
        lectures.put(lecture.getId(), lecture);
        return lecture;
    }

    public synchronized List<Lecture> saveAll(Iterable<Lecture> toSave) {
        List<Lecture> saved = new ArrayList<>();
        for (Lecture lecture : toSave) {
            saved.add(save(lecture));
        }
        return saved;
    }

    public synchronized List<Lecture> findAll() {
        return new ArrayList<>(lectures.values());
    }

    public synchronized Optional<Lecture> findById(String id) {
        return Optional.ofNullable(lectures.get(id));
    }

    public synchronized void deleteAll() {
        lectures.clear();
    }
}
