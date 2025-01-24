package com.adbdti.lessonsync.Services;

import com.adbdti.lessonsync.Model.Lecture;
import com.adbdti.lessonsync.Model.TimeTable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.adbdti.lessonsync.Repository.LectureRepository;
import com.adbdti.lessonsync.Repository.TimeTableRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opencensus.internal.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CalendarService {

    private final VertexAIService vertexAIService;

    private final LectureRepository lectureRepository;

    private final TimeTableRepository timeTableRepository;


    @Autowired
    public CalendarService(VertexAIService vertexAIService, LectureRepository lectureRepository, TimeTableRepository timeTableRepository) {
        this.vertexAIService = vertexAIService;
        this.lectureRepository = lectureRepository;
        this.timeTableRepository = timeTableRepository;
    }

    public static List<Map<String, String>> parseJson(String json) {

        Logger logger = LoggerFactory.getLogger(CalendarService.class);

        try{
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, new TypeReference<>() {
            });

        } catch(Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public TimeTable insertTimetable(String jsonString){

        List<Map<String, String>> timetableList = parseJson(jsonString);
        TimeTable timeTable = new TimeTable();
        timeTable = timeTableRepository.save(timeTable);

        if(timetableList == null || timetableList.isEmpty()){

            throw new IllegalArgumentException("Invalid timetable data");
        }

        List<Lecture> lectures = new ArrayList<>();
        System.out.println(timetableList);

        for (Map<String, String> timetableMap : timetableList) {
            Lecture lecture = new Lecture();
            if (timetableMap.get("course") == null) {
                throw new IllegalArgumentException("Course name is required");
            }

            lecture.setCourse(timetableMap.get("course"));
            lecture.setLecturerName(timetableMap.get("lecturer"));
            lecture.setStart_time(timetableMap.get("start_time"));
            lecture.setEnd_time(timetableMap.get("end_time"));
            lecture.setDay(timetableMap.get("day"));
            lecture.setLocation(timetableMap.get("location"));
            lecture.setGroupName(timetableMap.get("group"));

            lectures.add(lecture);

        }
        lectureRepository.saveAll(lectures);
        timeTable.setLectures(lectures);

        return timeTableRepository.save(timeTable);
    }



}
