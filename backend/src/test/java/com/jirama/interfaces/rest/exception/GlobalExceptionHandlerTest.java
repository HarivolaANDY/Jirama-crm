package com.jirama.interfaces.rest.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Direct unit test for {@link GlobalExceptionHandler}. Tests the
 * {@link GlobalExceptionHandler.ErrorResponse} and
 * {@link GlobalExceptionHandler.FieldErrorResponse} records, and exercises
 * the {@code buildErrorResponse} helper method through handler methods.
 * <p>
 * No Spring context required — the handler is instantiated directly.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler unit tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private WebRequest request;

    // ──────────────────────────────────────────────
    // ErrorResponse record
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("ErrorResponse record")
    class ErrorResponseRecord {

        @Test
        @DisplayName("constructor sets all fields correctly")
        void constructorSetsFields() {
            var now = Instant.now();
            var errors = List.of(new GlobalExceptionHandler.FieldErrorResponse("name", "is required"));

            var response = new GlobalExceptionHandler.ErrorResponse(
                    400, "BAD_REQUEST", "Invalid input", now, "/api/test", errors);

            assertThat(response.status()).isEqualTo(400);
            assertThat(response.code()).isEqualTo("BAD_REQUEST");
            assertThat(response.message()).isEqualTo("Invalid input");
            assertThat(response.timestamp()).isEqualTo(now);
            assertThat(response.path()).isEqualTo("/api/test");
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("name");
        }

        @Test
        @DisplayName("accepts null errors list")
        void nullErrors() {
            var response = new GlobalExceptionHandler.ErrorResponse(
                    500, "INTERNAL_ERROR", "Server error", Instant.now(), "/api/test", null);

            assertThat(response.errors()).isNull();
        }

        @Test
        @DisplayName("accepts empty errors list")
        void emptyErrors() {
            var response = new GlobalExceptionHandler.ErrorResponse(
                    400, "VALIDATION_ERROR", "Validation failed", Instant.now(), "/api/test", List.of());

            assertThat(response.errors()).isEmpty();
        }

        @Test
        @DisplayName("equals and hashCode work for identical records")
        void equalsAndHashCode() {
            var now = Instant.now();
            var r1 = new GlobalExceptionHandler.ErrorResponse(404, "NOT_FOUND", "Not found", now, "/x", null);
            var r2 = new GlobalExceptionHandler.ErrorResponse(404, "NOT_FOUND", "Not found", now, "/x", null);

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("equals returns false for different records")
        void notEquals() {
            var now = Instant.now();
            var r1 = new GlobalExceptionHandler.ErrorResponse(404, "NOT_FOUND", "Not found", now, "/x", null);
            var r2 = new GlobalExceptionHandler.ErrorResponse(200, "OK", "All good", now, "/x", null);

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString contains field values")
        void toStringContainsFields() {
            var response = new GlobalExceptionHandler.ErrorResponse(
                    403, "ACCESS_DENIED", "Access denied", Instant.parse("2026-01-01T00:00:00Z"),
                    "/admin", null);

            var str = response.toString();
            assertThat(str).contains("403");
            assertThat(str).contains("ACCESS_DENIED");
            assertThat(str).contains("Access denied");
            assertThat(str).contains("/admin");
        }
    }

    // ──────────────────────────────────────────────
    // FieldErrorResponse record
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("FieldErrorResponse record")
    class FieldErrorResponseRecord {

        @Test
        @DisplayName("constructor sets field and message")
        void constructorSetsFields() {
            var error = new GlobalExceptionHandler.FieldErrorResponse("email", "must be a valid email");

            assertThat(error.field()).isEqualTo("email");
            assertThat(error.message()).isEqualTo("must be a valid email");
        }

        @Test
        @DisplayName("equals and hashCode work for identical records")
        void equalsAndHashCode() {
            var e1 = new GlobalExceptionHandler.FieldErrorResponse("name", "is required");
            var e2 = new GlobalExceptionHandler.FieldErrorResponse("name", "is required");

            assertThat(e1).isEqualTo(e2);
            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }

        @Test
        @DisplayName("equals returns false for different fields")
        void notEquals() {
            var e1 = new GlobalExceptionHandler.FieldErrorResponse("name", "is required");
            var e2 = new GlobalExceptionHandler.FieldErrorResponse("email", "is required");

            assertThat(e1).isNotEqualTo(e2);
        }

        @Test
        @DisplayName("toString contains field and message")
        void toStringContainsFields() {
            var error = new GlobalExceptionHandler.FieldErrorResponse("phone", "already exists");

            var str = error.toString();
            assertThat(str).contains("phone");
            assertThat(str).contains("already exists");
        }
    }

    // ──────────────────────────────────────────────
    // buildErrorResponse (via handler methods)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("buildErrorResponse helper (exercised through handler methods)")
    class BuildErrorResponse {

        @Test
        @DisplayName("builds response with correct status, code, message, and path")
        void genericErrorResponse() {
            when(request.getDescription(false)).thenReturn("uri=/api/data");

            var response = handler.handleGeneric(new RuntimeException("test"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
            assertThat(response.getBody().message())
                    .isEqualTo("An unexpected error occurred. Please contact support.");
            assertThat(response.getBody().path()).isEqualTo("/api/data");
            assertThat(response.getBody().timestamp()).isNotNull();
            assertThat(response.getBody().errors()).isNull();
        }

        @Test
        @DisplayName("sets path by stripping uri= prefix from request description")
        void stripsUriPrefix() {
            when(request.getDescription(false)).thenReturn("uri=/invoices/42");

            var response = handler.handleNotFound(
                    new ResourceNotFoundException("Invoice", "FAC-001"), request);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().path()).isEqualTo("/invoices/42");
        }

        @Test
        @DisplayName("handles request description without uri= prefix")
        void noUriPrefix() {
            when(request.getDescription(false)).thenReturn("/direct/path");

            var response = handler.handleNotFound(
                    new ResourceNotFoundException("User", "1"), request);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().path()).isEqualTo("/direct/path");
        }

        @Test
        @DisplayName("forbidden response has correct status and code")
        void forbiddenResponse() {
            when(request.getDescription(false)).thenReturn("uri=/admin");

            var response = handler.handleAccessDenied(
                    new org.springframework.security.access.AccessDeniedException("denied"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(403);
            assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
            assertThat(response.getBody().message()).isEqualTo("Access denied");
        }

        @Test
        @DisplayName("not found response has correct status and code with exception message")
        void notFoundResponse() {
            when(request.getDescription(false)).thenReturn("uri=/invoices/FAC-001");

            var response = handler.handleNotFound(
                    new ResourceNotFoundException("Invoice", "FAC-001"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
            assertThat(response.getBody().message()).isEqualTo("Invoice not found: FAC-001");
        }

        @Test
        @DisplayName("conflict response uses custom code from BusinessRuleException")
        void conflictResponseWithCustomCode() {
            when(request.getDescription(false)).thenReturn("uri=/subscribers");

            var response = handler.handleBusinessRule(
                    new BusinessRuleException("DUPLICATE_PHONE", "Phone already exists"), request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().code()).isEqualTo("DUPLICATE_PHONE");
        }
    }
}
