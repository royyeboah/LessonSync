package com.adbdti.lessonsync.Services;


import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.google.cloud.vertexai.VertexAI;

import java.io.File;
import java.io.FileInputStream;

import java.util.Map;


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
            var text1 = """
                    Extract the schedule in this image into json format. Use different entries for start time and end time. \
                    Exclude time slot and replace the shorthand versions of the days with their full names\
                    Also, exclude the "schedule" and just return the schedule without it\
                    Give it to me directly like this {
                                                           "day": "Monday",
                                                           "start_time": "08:00",
                                                           "end_time": "08:55",
                                                           "course": "CSM 387",
                                                            "group": "1",
                                                           "lecturer": "D. ASAMOAH",
                                                            "location":"SCB-SF8"
                                                         },
                                                          {
                                                           "day": "Monday",
                                                            "start_time": "13:00",
                                                           "end_time": "13:55",
                                                           "course": "CSM 399",
                                                            "group": "1",
                                                           "lecturer": "L.A. BANNING",
                                                              "location":"SCB-GF17"
                                                         } and not like this "Monday": [
                        {
                          "start_time": "8:00",
                          "end_time": "8:55",
                          "course": "CSM 387",
                          "group": "Group 1",
                          "lecturer": "D. ASAMOAH",""";

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