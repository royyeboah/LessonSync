package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Services.VertexAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The uploaded timetable now lives in the student's own session. Two students uploading at the same
 * time must not see, edit, or publish each other's classes, since each of them has connected a
 * different Google account.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TimetableSessionIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VertexAIService vertexAIService;

    private static final String ADA_SCHEDULE = """
            [{"course":"Databases","lecturer":"Dr Ada","start_time":"09:00","end_time":"11:00",
              "day":"MONDAY","location":"Room A","group":"G1"}]
            """;

    private static final String BOB_SCHEDULE = """
            [{"course":"Networks","lecturer":"Dr Bob","start_time":"14:00","end_time":"16:00",
              "day":"TUESDAY","location":"Room B","group":"G2"}]
            """;

    @Test
    void oneStudentsUploadIsInvisibleToAnother() throws Exception {
        MockHttpSession ada = new MockHttpSession();
        MockHttpSession bob = new MockHttpSession();

        upload(ada, ADA_SCHEDULE);

        mockMvc.perform(get("/lectures").session(ada))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].course").value("Databases"));

        mockMvc.perform(get("/lectures").session(bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        upload(bob, BOB_SCHEDULE);

        mockMvc.perform(get("/lectures").session(bob))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].course").value("Networks"));

        // Ada's upload is untouched by Bob's.
        mockMvc.perform(get("/lectures").session(ada))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].course").value("Databases"));
    }

    @Test
    void editingALectureFromAnotherSessionIsNotPossible() throws Exception {
        MockHttpSession ada = new MockHttpSession();
        MockHttpSession bob = new MockHttpSession();

        upload(ada, ADA_SCHEDULE);
        String lectureId = mockMvc.perform(get("/lectures").session(ada))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/lecture/" + lectureId)
                        .session(bob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"course\":\"Hijacked\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/lectures").session(ada))
                .andExpect(jsonPath("$[0].course").value("Databases"));
    }

    @Test
    void theCurrentTimetableIsPerSession() throws Exception {
        MockHttpSession ada = new MockHttpSession();
        MockHttpSession bob = new MockHttpSession();

        upload(ada, ADA_SCHEDULE);

        mockMvc.perform(get("/timetable").session(ada)).andExpect(status().isOk());
        mockMvc.perform(get("/timetable").session(bob)).andExpect(status().isNotFound());
    }

    private void upload(MockHttpSession session, String extractedSchedule) throws Exception {
        given(vertexAIService.generateContent(any())).willReturn(extractedSchedule);

        mockMvc.perform(multipart("/lecture")
                        .file(new MockMultipartFile("file", "schedule.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "not-a-real-image".getBytes()))
                        .session(session))
                .andExpect(status().isOk());
    }
}
