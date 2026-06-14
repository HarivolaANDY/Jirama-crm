package com.jirama.interfaces.rest.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExceptionTestController.class)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerIntegrationTest.TestSecurityConfig.class})
@DisplayName("GlobalExceptionHandler — @WebMvcTest integration")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException → 404")
    class NotFound {

        @Test
        @DisplayName("returns 404 NOT_FOUND with correct code and message")
        void shouldReturn404() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Invoice not found: FAC-001"))
                    .andExpect(jsonPath("$.path").value("/test/not-found"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("BusinessRuleException → 409")
    class BusinessRule {

        @Test
        @DisplayName("returns 409 CONFLICT with custom code and message")
        void shouldReturn409() throws Exception {
            mockMvc.perform(get("/test/business-rule"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("DUPLICATE_PHONE"))
                    .andExpect(jsonPath("$.message").value("An active subscriber with this phone already exists"));
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException → 400")
    class BadRequest {

        @Test
        @DisplayName("returns 400 BAD_REQUEST with code BAD_REQUEST")
        void shouldReturn400() throws Exception {
            mockMvc.perform(get("/test/bad-request"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Invalid email format"));
        }
    }

    @Nested
    @DisplayName("IllegalStateException → 409")
    class InvalidState {

        @Test
        @DisplayName("returns 409 CONFLICT with code INVALID_STATE")
        void shouldReturn409() throws Exception {
            mockMvc.perform(get("/test/invalid-state"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("INVALID_STATE"))
                    .andExpect(jsonPath("$.message").value("Invoice is already PAID"));
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException → 400")
    class Validation {

        @Test
        @DisplayName("returns 400 BAD_REQUEST with field-level errors")
        void shouldReturn400WithFieldErrors() throws Exception {
            var invalidRequest = new ExceptionTestController.TestRequest("");

            mockMvc.perform(post("/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[0].field").value("name"))
                    .andExpect(jsonPath("$.errors[0].message").value("Le nom est obligatoire"))
                    .andExpect(jsonPath("$.path").value("/test/validation"));
        }
    }

    @Nested
    @DisplayName("AccessDeniedException → 403")
    class AccessDenied {

        @Test
        @DisplayName("returns 403 FORBIDDEN with code ACCESS_DENIED")
        void shouldReturn403() throws Exception {
            mockMvc.perform(get("/test/access-denied"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                    .andExpect(jsonPath("$.message").value("Access denied"));
        }
    }

    @Nested
    @DisplayName("AccountExpiredException → 401")
    class AccountExpiredError {

        @Test
        @DisplayName("returns 401 with code ACCOUNT_EXPIRED and specific message")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/account-expired"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("ACCOUNT_EXPIRED"))
                    .andExpect(jsonPath("$.message").value(
                            "Your account has expired. Please contact your administrator."));
        }
    }

    @Nested
    @DisplayName("CredentialsExpiredException → 401")
    class CredentialsExpiredError {

        @Test
        @DisplayName("returns 401 with code CREDENTIALS_EXPIRED and specific message")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/credentials-expired"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("CREDENTIALS_EXPIRED"))
                    .andExpect(jsonPath("$.message").value(
                            "Your credentials have expired. Please reset your password."));
        }
    }

    @Nested
    @DisplayName("DisabledException → 401")
    class DisabledError {

        @Test
        @DisplayName("returns 401 with code ACCOUNT_DISABLED and specific message")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/account-disabled"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"))
                    .andExpect(jsonPath("$.message").value(
                            "Your account has been disabled. Please contact your administrator."));
        }
    }

    @Nested
    @DisplayName("LockedException → 401")
    class LockedError {

        @Test
        @DisplayName("returns 401 with code ACCOUNT_LOCKED and specific message")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/account-locked"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"))
                    .andExpect(jsonPath("$.message").value(
                            "Your account has been locked. Please contact your administrator."));
        }
    }

    @Nested
    @DisplayName("AuthenticationServiceException → 500")
    class AuthServiceError {

        @Test
        @DisplayName("returns 500 with code AUTH_SERVICE_ERROR and generic message")
        void shouldReturn500() throws Exception {
            mockMvc.perform(get("/test/auth-service-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("AUTH_SERVICE_ERROR"))
                    .andExpect(jsonPath("$.message").value(
                            "An authentication service error occurred. Please try again later."));
        }
    }

    @Nested
    @DisplayName("InternalAuthenticationServiceException → 500")
    class InternalAuthServiceError {

        @Test
        @DisplayName("returns 500 with code INTERNAL_AUTH_ERROR and generic message")
        void shouldReturn500() throws Exception {
            mockMvc.perform(get("/test/internal-auth-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("INTERNAL_AUTH_ERROR"))
                    .andExpect(jsonPath("$.message").value(
                            "An internal authentication error occurred. Please try again later."));
        }
    }

    @Nested
    @DisplayName("InsufficientAuthenticationException → 401")
    class InsufficientAuthenticationError {

        @Test
        @DisplayName("returns 401 with code INSUFFICIENT_AUTHENTICATION and specific message")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/insufficient-authentication"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("INSUFFICIENT_AUTHENTICATION"))
                    .andExpect(jsonPath("$.message").value(
                            "The provided authentication credentials are not sufficient. A token with higher privileges is required."));
        }
    }

    @Nested
    @DisplayName("AuthenticationException → 401")
    class AuthenticationError {

        @Test
        @DisplayName("returns 401 UNAUTHORIZED with code UNAUTHORIZED")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/test/authentication-error"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
        }
    }

    @Nested
    @DisplayName("Generic Exception → 500")
    class GenericError {

        @Test
        @DisplayName("returns 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() throws Exception {
            mockMvc.perform(get("/test/generic-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."));
        }
    }

    @Nested
    @DisplayName("EmptyResultDataAccessException → 404")
    class EmptyResult {

        @Test
        @DisplayName("returns 404 NOT_FOUND with exception message")
        void shouldReturn404() throws Exception {
            mockMvc.perform(get("/test/empty-result"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Invoice not found"));
        }
    }

    @Nested
    @DisplayName("IncorrectResultSizeDataAccessException → 500")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
    class IncorrectResultSize {

        @Test
        @DisplayName("returns 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() throws Exception {
            mockMvc.perform(get("/test/incorrect-result-size"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("INCORRECT_RESULT_SIZE"))
                    .andExpect(jsonPath("$.message").value("A database query returned an unexpected number of results."));
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException → 409")
    class DataIntegrityViolation {

        @Test
        @DisplayName("returns 409 CONFLICT with generic message")
        void shouldReturn409() throws Exception {
            mockMvc.perform(get("/test/data-integrity"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.code").value("DATA_INTEGRITY_VIOLATION"))
                    .andExpect(jsonPath("$.message").value("The operation violated a database constraint."));
        }
    }

    @Nested
    @DisplayName("DataAccessException → 500")
    class DataAccess {

        @Test
        @DisplayName("returns 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() throws Exception {
            mockMvc.perform(get("/test/data-access"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.code").value("DATA_ACCESS_ERROR"))
                    .andExpect(jsonPath("$.message").value("A database error occurred. Please try again later."));
        }
    }
}
