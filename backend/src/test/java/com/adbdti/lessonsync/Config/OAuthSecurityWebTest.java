package com.adbdti.lessonsync.Config;

import com.adbdti.lessonsync.Controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(WebSecurityConfig.class)
@ImportAutoConfiguration(OAuth2ClientAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-google-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-google-client-secret",
        "spring.security.oauth2.client.registration.google.scope=openid,profile,email,https://www.googleapis.com/auth/calendar",
        "app.frontend-url=http://localhost:4200"
})
class OAuthSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void authStatusIsPublicAndAnonymousByDefault() throws Exception {
        mockMvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void authStatusReturnsTheSignedInGoogleProfile() throws Exception {
        mockMvc.perform(get("/api/auth/status").with(oauth2Login().attributes(attrs -> {
                    attrs.put("name", "Ada Lovelace");
                    attrs.put("email", "ada@example.com");
                    attrs.put("picture", "https://example.com/ada.png");
                })))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.name").value("Ada Lovelace"));
    }

    @Test
    void loginStartsTheGoogleAuthorizationCodeFlow() throws Exception {
        mockMvc.perform(get("/api/auth/login").param("returnTo", "/upload"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/oauth2/authorization/google"));
    }

    @Test
    void googleAuthorizationRedirectIncludesOfflineAccess() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("https://accounts.google.com/**"))
                .andExpect(result -> {
                    String location = result.getResponse().getHeader("Location");
                    org.junit.jupiter.api.Assertions.assertNotNull(location);
                    org.junit.jupiter.api.Assertions.assertTrue(location.contains("access_type=offline"));
                    org.junit.jupiter.api.Assertions.assertTrue(location.contains("prompt=consent"));
                    org.junit.jupiter.api.Assertions.assertTrue(location.contains("calendar"));
                });
    }

    @Test
    void calendarApiRequiresGoogleLogin() throws Exception {
        mockMvc.perform(get("/createEvents").param("reminderTime", "15"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/createCalendar").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
