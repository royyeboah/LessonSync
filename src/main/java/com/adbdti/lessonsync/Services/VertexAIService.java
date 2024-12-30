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

            File image1File = new File("C:\\Users\\roy\\Documents\\LessonSync\\src\\main\\resources\\static\\TESTTABLE.jpg");
            byte[] image1Bytes = new byte[(int) image1File.length()];
            try (FileInputStream image1FileInputStream = new FileInputStream(image1File)) {
                image1FileInputStream.read(image1Bytes);
            }
            var image1 = PartMaker.fromMimeTypeAndData(
                    "image/jpg", image1Bytes);
            var text1 = "Extract the schedule in this image into json format. Use different entries for start time and end time. " +
                    "Exclude time slot and replace the shorthand versions of the days with their full days";

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

            // Remove Markdown code blocks

            return jsonContent.toString()
                    .replaceAll("```(?:json)?|```", "") // Remove Markdown code blocks
                    .trim();

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

            File image1File = new File("C:\\Users\\roy\\Documents\\LessonSync\\src\\main\\resources\\static\\img.png");
            byte[] image1Bytes = new byte[(int) image1File.length()];
            try (FileInputStream image1FileInputStream = new FileInputStream(image1File)) {
                image1FileInputStream.read(image1Bytes);
            }
            var image1 = PartMaker.fromMimeTypeAndData(
                    "image/png", image1Bytes);
            var text1 = "What animal in this picture? Can you explain how the dog could get in that situation in the first place?";

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