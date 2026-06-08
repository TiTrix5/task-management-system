package com.example.taskmanagement.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String path = request.getRequestURI();

        // Make opening the root in browser friendly (no scary 401)
        if ("/".equals(path)) {
            response.setContentType("text/plain;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "Task Management System API is running.\n\n" +
                "Open /swagger-ui.html for full documentation and to test the API.\n" +
                "1. Register: POST /auth/register\n" +
                "2. Login: POST /auth/login\n" +
                "3. Use the returned token as Bearer in Authorization header for /tasks and /users/me"
            );
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = authException != null ? authException.getMessage() : "Full authentication is required";
        String body = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.LocalDateTime.now(),
            message.replace("\"", "'"),
            path
        );

        response.getWriter().write(body);
    }
}