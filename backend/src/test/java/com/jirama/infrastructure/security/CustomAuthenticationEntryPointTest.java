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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Direct unit test for {@link CustomAuthenticationEntryPoint}. Tests the entry
 * point in isolation with mocked servlet objects — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomAuthenticationEntryPoint unit tests")
class CustomAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    private final CustomAuthenticationEntryPoint entryPoint =
            new CustomAuthenticationEntryPoint(objectMapper);

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    /**
     * Sets up a working {@link ServletOutputStream} backed by a
     * {@link ByteArrayOutputStream} on the mocked {@code response}.
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

        private final AuthenticationException authException =
                new BadCredentialsException("Invalid token");

        @Test
        @DisplayName("sets 401 UNAUTHORIZED status")
        void setsUnauthorizedStatus() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            stubOutputStream();

            entryPoint.commence(request, response, authException);

            verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("sets application/json content type")
        void setsJsonContentType() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            stubOutputStream();

            entryPoint.commence(request, response, authException);

            verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        }

        @Test
        @DisplayName("sets WWW-Authenticate header with Bearer realm and error description")
        void setsWwwAuthenticateHeader() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            stubOutputStream();

            entryPoint.commence(request, response, authException);

            verify(response).setHeader("WWW-Authenticate",
                    "Bearer realm=\"jirama\", error=\"invalid_token\", " +
                    "error_description=\"Authentication is required to access this resource.\"");
        }

        @Test
        @DisplayName("writes ErrorResponse JSON to the output stream")
        void writesErrorResponseToOutputStream() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, authException);

            var json = outputStream.toString();
            var parsed = objectMapper.readTree(json);
            assertThat(parsed.get("status").asInt()).isEqualTo(401);
            assertThat(parsed.get("code").asText()).isEqualTo("UNAUTHORIZED");
            assertThat(parsed.get("message").asText())
                    .isEqualTo("Authentication is required to access this resource.");
            assertThat(parsed.get("path").asText()).isEqualTo("/api/test");
            assertThat(parsed.get("timestamp")).isNotNull();
        }
    }

    @Nested
    @DisplayName("ErrorResponse JSON content")
    class ErrorResponseContent {

        private final AuthenticationException authException =
                new BadCredentialsException("Invalid token");

        @Test
        @DisplayName("includes all required fields with correct values")
        void includesAllRequiredFields() throws IOException {
            when(request.getRequestURI()).thenReturn("/secure/data");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, authException);

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("status").asInt()).isEqualTo(401);
            assertThat(parsed.get("code").asText()).isEqualTo("UNAUTHORIZED");
            assertThat(parsed.get("message").asText())
                    .isEqualTo("Authentication is required to access this resource.");
            assertThat(parsed.get("path").asText()).isEqualTo("/secure/data");
            assertThat(parsed.get("timestamp")).isNotNull();
            assertThat(parsed.has("errors")).isTrue();
            assertThat(parsed.get("errors").isNull()).isTrue();
        }

        @Test
        @DisplayName("uses request URI for path")
        void usesRequestUriForPath() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/invoices");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, authException);

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("path").asText()).isEqualTo("/api/invoices");
        }

        @Test
        @DisplayName("handles request with root URI")
        void handlesRootUri() throws IOException {
            when(request.getRequestURI()).thenReturn("/");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, authException);

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("path").asText()).isEqualTo("/");
        }

        @Test
        @DisplayName("handles request with null URI gracefully")
        void handlesNullUri() throws IOException {
            when(request.getRequestURI()).thenReturn(null);
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, authException);

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.has("path")).isTrue();
            assertThat(parsed.get("path").isNull()).isTrue();
        }
    }

    @Nested
    @DisplayName("Exception type handling")
    class ExceptionTypeHandling {

        @Test
        @DisplayName("handles BadCredentialsException without failing")
        void handlesBadCredentials() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response, new BadCredentialsException("Invalid token"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("status").asInt()).isEqualTo(401);
            assertThat(parsed.get("code").asText()).isEqualTo("UNAUTHORIZED");
        }

        @Test
        @DisplayName("handles InsufficientAuthenticationException without failing")
        void handlesInsufficientAuthentication() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            var outputStream = stubOutputStream();

            entryPoint.commence(request, response,
                    new InsufficientAuthenticationException("Missing scope"));

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("status").asInt()).isEqualTo(401);
            assertThat(parsed.get("code").asText()).isEqualTo("UNAUTHORIZED");
        }
    }

    @Nested
    @DisplayName("Edge cases and error handling")
    class EdgeCases {

        @Test
        @DisplayName("rejects null request with NullPointerException")
        void nullRequest() {
            assertThrows(NullPointerException.class,
                    () -> entryPoint.commence(null, response,
                            new BadCredentialsException("test")));
        }

        @Test
        @DisplayName("rejects null response with NullPointerException")
        void nullResponse() {
            assertThrows(NullPointerException.class,
                    () -> entryPoint.commence(request, null,
                            new BadCredentialsException("test")));
        }

        @Test
        @DisplayName("propagates IOException from output stream")
        void ioExceptionFromOutputStream() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            when(response.getOutputStream()).thenThrow(new IOException("Stream closed"));

            assertThrows(IOException.class,
                    () -> entryPoint.commence(request, response,
                            new BadCredentialsException("test")));
        }

        @Test
        @DisplayName("handles any AuthenticationException subclass")
        void handlesAnyAuthException() throws IOException {
            when(request.getRequestURI()).thenReturn("/api/test");
            var outputStream = stubOutputStream();

            // Custom anonymous subclass to verify the handler doesn't depend on
            // specific exception types
            entryPoint.commence(request, response, new AuthenticationException("Custom") {});

            var parsed = objectMapper.readTree(outputStream.toString());
            assertThat(parsed.get("code").asText()).isEqualTo("UNAUTHORIZED");
        }
    }
}
