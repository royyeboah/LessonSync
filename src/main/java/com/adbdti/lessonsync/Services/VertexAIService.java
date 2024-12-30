package com.adbdti.lessonsync.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.json.Json;
import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import com.google.cloud.vertexai.VertexAI;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.catalina.manager.StatusTransformer.formatTime;

@Service
public class VertexAIService {

    Logger logger = LoggerFactory.getLogger(VertexAIService.class);

    public String generateContent() {
        try (VertexAI vertexAi = new VertexAI("class-scheduler-429214", "us-central1");) {

            GenerativeModel model = new GenerativeModel("gemini-2.0-flash-exp", vertexAi);

            File image1File = new File("C:\\Users\\nserviceebanking\\IdeaProjects\\LessonSync\\src\\main\\resources\\static\\TESTTABLE.jpg");
            byte[] image1Bytes = new byte[(int) image1File.length()];
            try (FileInputStream image1FileInputStream = new FileInputStream(image1File)) {
                image1FileInputStream.read(image1Bytes);
            }
            var image1 = PartMaker.fromMimeTypeAndData(
                    "image/jpg", image1Bytes);
            var text1 = "Can you extract the schedule in the image in json format can you add new entries in the json for start time and end time";

            var content = ContentMaker.fromMultiModalData(image1, text1);

            ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(content);

            StringBuilder jsonContent = new StringBuilder();
            responseStream.stream().forEach(response ->
            {
                response.getCandidatesList().forEach(candidate -> {
                    candidate.getContent().getPartsList().forEach(part -> {
                        if (part.hasText()) {
                            jsonContent.append(part.getText());
                        }
                    });
                });
            });

            String cleanJson = jsonContent.toString()
                    .replaceAll("```(?:json)?|```", "") // Remove Markdown code blocks
                    .trim();

            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode rootNode = mapper.readTree(cleanJson);
                JsonNode scheduleArray = rootNode.get("schedule");

                ObjectNode result = mapper.createObjectNode();
                ObjectNode scheduleNode = result.putObject("schedule");

                Map<String, List<JsonNode>> groupedByDay = StreamSupport
                        .stream(scheduleArray.spliterator(), false)
                        .collect(Collectors.groupingBy(item ->
                                getDayName(item.get("day").asText())));

                groupedByDay.forEach((day, items) -> {
                    ArrayNode dayArray = scheduleNode.putArray(day.toLowerCase());
                    items.stream()
                            .sorted(Comparator.comparing(item -> item.get("start_time").asText()))
                            .forEach(item -> {
                                ObjectNode entry = dayArray.addObject();
                                entry.put("course", item.get("course").asText());

                                // Handle optional group field
                                if (item.has("group") && !item.get("group").isNull()) {
                                    entry.put("group", item.get("group").asText().split(" ")[1]);
                                }

                                entry.put("lecturer", item.get("lecturer").asText());

                                // Handle optional location field
                                if (item.has("location") && !item.get("location").isNull()) {
                                    entry.put("location", item.get("location").asText());
                                }

                                entry.put("start_time", formatTime(item.get("start_time").asText()));
                                entry.put("end_time", formatTime(item.get("end_time").asText()));
                            });
                });

                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            } catch (JsonProcessingException e) {
                logger.error("Failed to parse JSON:{}", cleanJson);
                logger.error(e.getMessage(), e);
                throw new RuntimeException("Failed to parse schedule", e);
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }

    private String getDayName(String day) {
        Map<String, String> dayMap = Map.of(
                "Mo", "monday",
                "Tu", "tuesday",
                "We", "wednesday",
                "Th", "thursday",
                "Fr", "friday"
        );
        return dayMap.get(day);
    }

    private String formatTime(String time) {
        return time.replaceFirst("^0", "");
    }

    public String getRawResponse() {
        try (VertexAI vertexAi = new VertexAI("class-scheduler-429214", "us-central1")) {
            GenerativeModel model = new GenerativeModel("gemini-2.0-flash-exp", vertexAi);

            File image1File = new File("C:\\Users\\nserviceebanking\\IdeaProjects\\LessonSync\\src\\main\\resources\\static\\TESTTABLE.jpg");
            byte[] image1Bytes = new byte[(int) image1File.length()];
            try (FileInputStream image1FileInputStream = new FileInputStream(image1File)) {
                image1FileInputStream.read(image1Bytes);
            }
            var image1 = PartMaker.fromMimeTypeAndData(
                    "image/jpg", image1Bytes);
            var text1 = "Can you extract the schedule in the image in json format can you add new entries in the json for start time and end time";

            var content = ContentMaker.fromMultiModalData(image1, text1);
            ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(content);

            StringBuilder debug = new StringBuilder();
            responseStream.stream().forEach(response -> {
                debug
                        .append(response.toString());
            });

            return debug.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}