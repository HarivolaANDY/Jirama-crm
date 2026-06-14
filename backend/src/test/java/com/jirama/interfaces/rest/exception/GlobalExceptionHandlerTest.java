package com.jirama.interfaces.rest.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        // Return a predictable path for all tests
        lenient().when(request.getDescription(false)).thenReturn("uri=/api/v1/test");
    }

    private void assertStatus(HttpStatus expected, ResponseEntity<?> response) {
        assertThat(response.getStatusCode()).isEqualTo(expected);
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<GlobalExceptionHandler.ErrorResponse> cast(ResponseEntity<?> response) {
        return (ResponseEntity<GlobalExceptionHandler.ErrorResponse>) response;
    }

    private void assertErrorResponse(
            ResponseEntity<GlobalExceptionHandler.ErrorResponse> response,
            int expectedStatus,
            String expectedCode,
            String expectedMessage) {

        GlobalExceptionHandler.ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(expectedStatus);
        assertThat(body.code()).isEqualTo(expectedCode);
        assertThat(body.message()).isEqualTo(expectedMessage);
        assertThat(body.path()).isEqualTo("/api/v1/test");
        assertThat(body.timestamp()).isNotNull();
    }

    @Nested
    @DisplayName("ResourceNotFoundException → 404")
    class ResourceNotFound {

        @Test
        @DisplayName("maps to 404 NOT_FOUND with code NOT_FOUND")
        void shouldReturn404() {
            var ex = new ResourceNotFoundException("Invoice", "FAC-001");
            var response = cast(handler.handleNotFound(ex, request));

            assertStatus(HttpStatus.NOT_FOUND, response);
            assertErrorResponse(response, 404, "NOT_FOUND", "Invoice not found: FAC-001");
        }
    }

    @Nested
    @DisplayName("BusinessRuleException → 409")
    class BusinessRule {

        @Test
        @DisplayName("maps to 409 CONFLICT with the exception's code")
        void shouldReturn409() {
            var ex = new BusinessRuleException("DUPLICATE_PHONE", "An active subscriber with this phone already exists");
            var response = cast(handler.handleBusinessRule(ex, request));

            assertStatus(HttpStatus.CONFLICT, response);
            assertErrorResponse(response, 409, "DUPLICATE_PHONE", "An active subscriber with this phone already exists");
        }

        @Test
        @DisplayName("maps different codes correctly")
        void shouldPreserveCustomCode() {
            var ex = new BusinessRuleException("INVALID_STATUS_TRANSITION", "Cannot cancel a paid invoice");
            var response = cast(handler.handleBusinessRule(ex, request));

            assertErrorResponse(response, 409, "INVALID_STATUS_TRANSITION", "Cannot cancel a paid invoice");
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException → 400")
    class IllegalArgument {

        @Test
        @DisplayName("maps to 400 BAD_REQUEST with code BAD_REQUEST")
        void shouldReturn400() {
            var ex = new IllegalArgumentException("Invalid email format");
            var response = cast(handler.handleIllegalArgument(ex, request));

            assertStatus(HttpStatus.BAD_REQUEST, response);
            assertErrorResponse(response, 400, "BAD_REQUEST", "Invalid email format");
        }
    }

    @Nested
    @DisplayName("IllegalStateException → 409")
    class IllegalState {

        @Test
        @DisplayName("maps to 409 CONFLICT with code INVALID_STATE")
        void shouldReturn409() {
            var ex = new IllegalStateException("Invoice is already PAID");
            var response = cast(handler.handleIllegalState(ex, request));

            assertStatus(HttpStatus.CONFLICT, response);
            assertErrorResponse(response, 409, "INVALID_STATE", "Invoice is already PAID");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException → 400")
    class Validation {

        @Test
        @DisplayName("maps to 400 BAD_REQUEST with field-level errors")
        void shouldReturn400WithFieldErrors() throws Exception {
            // Create a mock BindingResult with field errors
            var bindingResult = org.mockito.Mockito.mock(BindingResult.class);
            var fieldErrors = List.of(
                    new FieldError("registerRequest", "firstName", "Le prénom est obligatoire"),
                    new FieldError("registerRequest", "email", "Format d'email invalide")
            );
            when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

            // MockMethodArgumentNotValidException needs a valid constructor
            var ex = new MethodArgumentNotValidException(null, bindingResult);
            var response = cast(handler.handleValidation(ex, request));

            assertStatus(HttpStatus.BAD_REQUEST, response);
            GlobalExceptionHandler.ErrorResponse body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.status()).isEqualTo(400);
            assertThat(body.code()).isEqualTo("VALIDATION_ERROR");
            assertThat(body.message()).isEqualTo("Validation failed");
            assertThat(body.errors()).isNotNull();
            assertThat(body.errors()).hasSize(2);

            assertThat(body.errors().get(0).field()).isEqualTo("firstName");
            assertThat(body.errors().get(0).message()).isEqualTo("Le prénom est obligatoire");
            assertThat(body.errors().get(1).field()).isEqualTo("email");
            assertThat(body.errors().get(1).message()).isEqualTo("Format d'email invalide");
        }

        @Test
        @DisplayName("handles empty field error list")
        void shouldHandleEmptyErrors() throws Exception {
            var bindingResult = org.mockito.Mockito.mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            var ex = new MethodArgumentNotValidException(null, bindingResult);
            var response = cast(handler.handleValidation(ex, request));

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().errors()).isEmpty();
        }
    }

    @Nested
    @DisplayName("EmptyResultDataAccessException → 404")
    class EmptyResult {

        @Test
        @DisplayName("maps to 404 NOT_FOUND with code NOT_FOUND")
        void shouldReturn404() {
            var ex = new EmptyResultDataAccessException("Invoice not found", 1);
            var response = cast(handler.handleEmptyResult(ex, request));

            assertStatus(HttpStatus.NOT_FOUND, response);
            assertErrorResponse(response, 404, "NOT_FOUND", "Invoice not found");
        }
    }

    @Nested
    @DisplayName("IncorrectResultSizeDataAccessException → 500")
    class IncorrectResultSize {

        @Test
        @DisplayName("maps to 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() {
            var ex = new IncorrectResultSizeDataAccessException("Expected 1 but found 3", 1, 3);
            var response = cast(handler.handleIncorrectResultSize(ex, request));

            assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, response);
            assertErrorResponse(response, 500, "INCORRECT_RESULT_SIZE",
                    "A database query returned an unexpected number of results.");
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException → 409")
    class DataIntegrityViolation {

        @Test
        @DisplayName("maps to 409 CONFLICT with code DATA_INTEGRITY_VIOLATION")
        void shouldReturn409() {
            var ex = new DataIntegrityViolationException("Unique constraint violation on column email");
            var response = cast(handler.handleDataIntegrity(ex, request));

            assertStatus(HttpStatus.CONFLICT, response);
            assertErrorResponse(response, 409, "DATA_INTEGRITY_VIOLATION",
                    "The operation violated a database constraint.");
        }
    }

    @Nested
    @DisplayName("DataAccessException → 500")
    class DataAccess {

        @Test
        @DisplayName("maps to 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() {
            var ex = new DataAccessException("Connection pool exhausted") {};
            var response = cast(handler.handleDataAccess(ex, request));

            assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, response);
            assertErrorResponse(response, 500, "DATA_ACCESS_ERROR",
                    "A database error occurred. Please try again later.");
        }
    }

    @Nested
    @DisplayName("AccessDeniedException → 403")
    class AccessDenied {

        @Test
        @DisplayName("maps to 403 FORBIDDEN with code ACCESS_DENIED")
        void shouldReturn403() {
            var ex = new AccessDeniedException("Access denied");
            var response = cast(handler.handleAccessDenied(ex, request));

            assertStatus(HttpStatus.FORBIDDEN, response);
            assertErrorResponse(response, 403, "ACCESS_DENIED", "Access denied");
        }
    }

    @Nested
    @DisplayName("Generic Exception → 500")
    class GenericException {

        @Test
        @DisplayName("maps to 500 INTERNAL_SERVER_ERROR with generic message")
        void shouldReturn500() {
            var ex = new RuntimeException("Something went wrong in the database");
            var response = cast(handler.handleGeneric(ex, request));

            assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, response);
            assertErrorResponse(response, 500, "INTERNAL_ERROR", "An unexpected error occurred. Please contact support.");
        }
    }

    @Nested
    @DisplayName("ErrorResponse record")
    class ErrorResponseRecord {

        @Test
        @DisplayName("constructs correctly with all fields")
        void shouldConstruct() {
            var errors = List.of(new GlobalExceptionHandler.FieldErrorResponse("email", "invalid"));
            var response = new GlobalExceptionHandler.ErrorResponse(400, "BAD_REQUEST", "Bad request", java.time.Instant.now(), "/api/test", errors);

            assertThat(response.status()).isEqualTo(400);
            assertThat(response.code()).isEqualTo("BAD_REQUEST");
            assertThat(response.message()).isEqualTo("Bad request");
            assertThat(response.path()).isEqualTo("/api/test");
            assertThat(response.errors()).hasSize(1);
            assertThat(response.errors().get(0).field()).isEqualTo("email");
        }
    }

    @Nested
    @DisplayName("Complete exception-to-status mapping summary")
    class MappingSummary {

        @Test
        @DisplayName("all exceptions produce consistent ErrorResponse structure")
        void allMappingsProduceConsistentStructure() {
            // ResourceNotFoundException → 404
            var rnf = cast(handler.handleNotFound(new ResourceNotFoundException("X", "1"), request));
            assertThat(rnf.getBody()).isNotNull();
            assertThat(rnf.getBody().status()).isEqualTo(404);

            // BusinessRuleException → 409
            var bre = cast(handler.handleBusinessRule(new BusinessRuleException("X", "Y"), request));
            assertThat(bre.getBody()).isNotNull();
            assertThat(bre.getBody().status()).isEqualTo(409);

            // IllegalArgumentException → 400
            var iae = cast(handler.handleIllegalArgument(new IllegalArgumentException("X"), request));
            assertThat(iae.getBody()).isNotNull();
            assertThat(iae.getBody().status()).isEqualTo(400);

            // IllegalStateException → 409
            var ise = cast(handler.handleIllegalState(new IllegalStateException("X"), request));
            assertThat(ise.getBody()).isNotNull();
            assertThat(ise.getBody().status()).isEqualTo(409);

            // AccessDeniedException → 403
            var ade = cast(handler.handleAccessDenied(new AccessDeniedException("X"), request));
            assertThat(ade.getBody()).isNotNull();
            assertThat(ade.getBody().status()).isEqualTo(403);

            // Generic Exception → 500
            var ge = cast(handler.handleGeneric(new RuntimeException("X"), request));
            assertThat(ge.getBody()).isNotNull();
            assertThat(ge.getBody().status()).isEqualTo(500);

            // EmptyResultDataAccessException → 404
            var ere = cast(handler.handleEmptyResult(new EmptyResultDataAccessException("X", 1), request));
            assertThat(ere.getBody()).isNotNull();
            assertThat(ere.getBody().status()).isEqualTo(404);

            // IncorrectResultSizeDataAccessException → 500
            var irs = cast(handler.handleIncorrectResultSize(new IncorrectResultSizeDataAccessException("X", 1, 0), request));
            assertThat(irs.getBody()).isNotNull();
            assertThat(irs.getBody().status()).isEqualTo(500);

            // DataIntegrityViolationException → 409
            var div = cast(handler.handleDataIntegrity(new DataIntegrityViolationException("X"), request));
            assertThat(div.getBody()).isNotNull();
            assertThat(div.getBody().status()).isEqualTo(409);

            // DataAccessException → 500
            var dae = cast(handler.handleDataAccess(new DataAccessException("X") {}, request));
            assertThat(dae.getBody()).isNotNull();
            assertThat(dae.getBody().status()).isEqualTo(500);

            // All responses have timestamp, path, and no errors field for non-validation
            var all = List.of(rnf, bre, iae, ise, ade, ge, ere, irs, div, dae);
            for (var r : all) {
                assertThat(r.getBody().timestamp()).isNotNull();
                assertThat(r.getBody().path()).isEqualTo("/api/v1/test");
                assertThat(r.getBody().errors()).isNull();
            }
        }
    }
}
