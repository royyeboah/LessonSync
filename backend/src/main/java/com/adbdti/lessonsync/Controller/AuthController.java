package com.adbdti.lessonsync.Controller;

import com.adbdti.lessonsync.Auth.AuthStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String REDIRECT_AFTER_LOGIN = "redirect_after_login";

    private final String frontendUrl;

    public AuthController(@Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/status")
    public AuthStatus status(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return AuthStatus.anonymous();
        }
        return AuthStatus.of(
                user.getAttribute("name"),
                user.getAttribute("email"),
                user.getAttribute("picture")
        );
    }

    @GetMapping("/login")
    public void login(@RequestParam(required = false) String returnTo,
                      HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        request.getSession().setAttribute(REDIRECT_AFTER_LOGIN, safeFrontendRedirect(returnTo));
        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/google");
    }

    String safeFrontendRedirect(String returnTo) {
        if (returnTo == null || !returnTo.startsWith("/") || returnTo.startsWith("//") || returnTo.contains("://")) {
            return frontendUrl + "/";
        }
        return frontendUrl + returnTo;
    }
}
