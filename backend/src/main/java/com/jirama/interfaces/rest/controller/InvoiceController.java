package com.jirama.interfaces.rest.controller;

import com.jirama.application.billing.CustomerDashboardUseCase;
import com.jirama.application.billing.GetMyInvoicesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@Tag(name = "Invoices", description = "Invoice and billing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final GetMyInvoicesUseCase getMyInvoicesUseCase;
    private final CustomerDashboardUseCase customerDashboardUseCase;

    public InvoiceController(GetMyInvoicesUseCase getMyInvoicesUseCase,
                              CustomerDashboardUseCase customerDashboardUseCase) {
        this.getMyInvoicesUseCase = getMyInvoicesUseCase;
        this.customerDashboardUseCase = customerDashboardUseCase;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get invoice by ID")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        // TODO: Implement get invoice use case
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping(value = "/my/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current unpaid invoices for the authenticated customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyCurrentInvoices(@AuthenticationPrincipal Jwt jwt) {
        UUID subscriberId = UUID.fromString(jwt.getSubject());
        var invoices = getMyInvoicesUseCase.getCurrent(subscriberId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all invoices for the authenticated customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<GetMyInvoicesUseCase.InvoiceSummary>> getMyInvoices(
            @AuthenticationPrincipal Jwt jwt) {
        UUID subscriberId = UUID.fromString(jwt.getSubject());
        var invoices = getMyInvoicesUseCase.getAll(subscriberId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping(value = "/{id}/pdf", produces = "application/pdf")
    @Operation(summary = "Download invoice PDF")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
    public ResponseEntity<?> downloadPdf(@PathVariable UUID id) {
        // TODO: Implement PDF generation from template
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
