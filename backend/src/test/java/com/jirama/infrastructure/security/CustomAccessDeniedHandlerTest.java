package com.jirama.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Direct unit test for {@link CustomAccessDeniedHandler}. Tests the handler
 * in isolation with mocked servlet objects — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAccessDeniedHandler unit tests")
class CustomAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler(objectMapper);

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    /**
     * Sets up a working {@link ServletOutputStream} backed by a
     * {@link ByteArrayOutputStream} on the mocked {@code response}.
     * Each test that needs to capture the JSON output calls this first.
     */
    private ByteArrayOutputStream stubOutputStream() throws IOException {
        var baos = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) {
                baos.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
            }
        });
        return baos;
    }

    @Nested
    @DisplayName("HTTP response setup")
    class ResponseSetup {

        @Test
        @DisplayName("sets 403 FORBIDDEN status")
        void setsForbiddenStatus() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("sets application/json content type")
        void setsJsonContentType() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("writes ErrorResponse JSON to the output stream")
        void writesErrorResponseToOutputStream() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            var outputStream = stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            var json = outputStream.toString();
            var parsed = objectMapper.readTree(json);
            assertThat(parsed.get("status").asInt()).isEqualTo(403);
            assertThat(parsed.get("code").asText()).isEqualTo("ACCESS_DENIED");
            assertThat(parsed.get("message").asText()).isEqualTo("Access denied");
            assertThat(parsed.get("path").asText()).isEqualTo("/api/test");
            assertThat(parsed.get("timestamp")).isNotNull();
        }
    }

    @Nested
    @DisplayName("ErrorResponse JSON content")
    class ErrorResponseContent {

        @Test
        @DisplayName("includes all required fields with correct values")
        void includesAllRequiredFields() throws IOException {
            when(request.getRequestURI()).thenReturn("/admin/settings");
            var outputStream = stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("status").asInt()).isEqualTo(403);
            assertThat(parsed.get("code").asText()).isEqualTo("ACCESS_DENIED");
            assertThat(parsed.get("message").asText()).isEqualTo("Access denied");
            assertThat(parsed.get("path").asText()).isEqualTo("/admin/settings");
            assertThat(parsed.get("timestamp")).isNotNull();
            assertThat(parsed.has("errors")).isTrue();
            assertThat(parsed.get("errors").isNull()).isTrue();
        }

        @Test
        @DisplayName("uses request URI for path")
        void usesRequestUriForPath() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/invoices/123");
            var outputStream = stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("path").asText()).isEqualTo("/api/invoices/123");
        }

        @Test
        @DisplayName("handles request with root URI")
        void handlesRootUri() throws IOException {
            when(request.getRequestURI()).thenReturn("/");
            var outputStream = stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("path").asText()).isEqualTo("/");
        }

        @Test
        @DisplayName("handles request with null URI gracefully")
        void handlesNullUri() throws IOException {
            when(request.getRequestURI()).thenReturn(null);
            var outputStream = stubOutputStream();

            handler.handle(request, response, new AccessDeniedException("Access denied"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.has("path")).isTrue();
            assertThat(parsed.get("path").isNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("Exception handling")
    class ExceptionHandling {

        @Test
        @DisplayName("rejects null request with NullPointerException")
        void nullRequest() {
            assertThrows(NullPointerException.class,
                    () -> handler.handle(null, response, new AccessDeniedException("Access denied")));
        }

        @Test
        @DisplayName("rejects null response with NullPointerException")
        void nullResponse() {
            assertThrows(NullPointerException.class,
                    () -> handler.handle(request, null, new AccessDeniedException("Access denied")));
        }

        @Test
        @DisplayName("propagates IOException from output stream")
        void ioExceptionFromOutputStream() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(response.getOutputStream()).thenThrow(new IOException("Stream closed"));

            assertThrows(IOException.class,
                    () -> handler.handle(request, response, new AccessDeniedException("Access denied")));
        }
    }
}
