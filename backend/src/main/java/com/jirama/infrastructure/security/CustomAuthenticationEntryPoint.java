package com.jirama.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirama.interfaces.rest.exception.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Custom {@link AuthenticationEntryPoint} that produces the same JSON error
 * response format as {@link GlobalExceptionHandler}, ensuring consistent 401
 * responses whether the authentication failure happens at the filter level
 * or the controller level.
 * <p>
 * This is invoked by Spring Security's {@code ExceptionTranslationFilter}
 * when an {@link AuthenticationException} is thrown at the filter level
 * (before the request reaches the {@code DispatcherServlet}).
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("WWW-Authenticate",
                "Bearer realm=\"jirama\", error=\"invalid_token\", error_description=\"Authentication is required to access this resource.\"");

        var errorResponse = new GlobalExceptionHandler.ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Authentication is required to access this resource.",
                Instant.now(),
                request.getRequestURI(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
