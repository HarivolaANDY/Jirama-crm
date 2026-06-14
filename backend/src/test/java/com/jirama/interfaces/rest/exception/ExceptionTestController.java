package com.jirama.interfaces.rest.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller that throws each exception type handled by GlobalExceptionHandler.
 * Used by GlobalExceptionHandlerIntegrationTest to verify end-to-end behavior.
 */
@RestController
@RequestMapping("/test")
public class ExceptionTestController {

    @GetMapping("/not-found")
    public void throwNotFound() {
        throw new ResourceNotFoundException("Invoice", "FAC-001");
    }

    @GetMapping("/business-rule")
    public void throwBusinessRule() {
        throw new BusinessRuleException("DUPLICATE_PHONE", "An active subscriber with this phone already exists");
    }

    @GetMapping("/bad-request")
    public void throwBadRequest() {
        throw new IllegalArgumentException("Invalid email format");
    }

    @GetMapping("/invalid-state")
    public void throwInvalidState() {
        throw new IllegalStateException("Invoice is already PAID");
    }

    @PostMapping("/validation")
    public void triggerValidation(@Valid @RequestBody TestRequest request) {
        // MethodArgumentNotValidException thrown automatically if validation fails
    }

    @GetMapping("/access-denied")
    public void throwAccessDenied() {
        throw new AccessDeniedException("Access denied");
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public void adminOnly() {
        // Only accessible to users with ADMIN role
    }

    @GetMapping("/permission/invoice-pay")
    @PreAuthorize("hasPermission('INVOICE', 'PAY')")
    public void permissionInvoicePay() {
        // Only accessible to CUSTOMER and ADMIN roles
    }

    @GetMapping("/permission/subscriber-read")
    @PreAuthorize("hasPermission('SUBSCRIBER', 'READ')")
    public void permissionSubscriberRead() {
        // Accessible to all authenticated roles
    }

    @GetMapping("/permission/admin-panel")
    @PreAuthorize("hasPermission('ADMIN_PANEL', 'ACCESS')")
    public void permissionAdminPanel() {
        // Only ADMIN can access
    }

    @GetMapping("/permission/incident-report")
    @PreAuthorize("hasPermission('INCIDENT', 'REPORT')")
    public void permissionIncidentReport() {
        // Only CUSTOMER and TECHNICIAN can report incidents
    }

    @GetMapping("/authentication-error")
    public void throwAuthentication() {
        throw new BadCredentialsException("Invalid or expired JWT token");
    }

    @GetMapping("/insufficient-authentication")
    public void throwInsufficientAuthentication() {
        throw new InsufficientAuthenticationException("Token does not have the required scope");
    }

    @GetMapping("/generic-error")
    public void throwGeneric() {
        throw new RuntimeException("Something went wrong in the database");
    }

    @GetMapping("/empty-result")
    public void throwEmptyResult() {
        throw new EmptyResultDataAccessException("Invoice not found", 1);
    }

    @GetMapping("/incorrect-result-size")
    public void throwIncorrectResultSize() {
        throw new IncorrectResultSizeDataAccessException("Expected 1 but found 3", 1, 3);
    }

    @GetMapping("/data-integrity")
    public void throwDataIntegrity() {
        throw new DataIntegrityViolationException("Unique constraint violation on column email");
    }

    @GetMapping("/data-access")
    public void throwDataAccess() {
        throw new DataAccessException("Connection pool exhausted") {};
    }

    /** DTO for validation testing — triggers MethodArgumentNotValidException when name is blank. */
    public record TestRequest(@NotBlank(message = "Le nom est obligatoire") String name) {}
}
