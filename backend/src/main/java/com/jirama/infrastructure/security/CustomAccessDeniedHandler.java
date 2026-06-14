package com.jirama.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirama.interfaces.rest.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom {@link AccessDeniedHandler} that produces the same JSON error response
 * format as {@link GlobalExceptionHandler}, ensuring consistent error responses
 * whether the denial happens at the filter level or the controller level.
 *
 * This is invoked by Spring Security's {@code ExceptionTranslationFilter}
 * when an {@link AccessDeniedException} is thrown at the filter level
 * (outside the {@code DispatcherServlet}).
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var errorResponse = new GlobalExceptionHandler.ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "Access denied",
                Instant.now(),
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
