package com.jirama.interfaces.rest.controller;

import com.jirama.application.billing.ProcessPaymentUseCase;
import com.jirama.domain.billing.enums.PaymentMethod;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.interfaces.rest.dto.request.ProcessPaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Payment processing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final SubscriberRepository subscriberRepository;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase,
                              SubscriberRepository subscriberRepository) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.subscriberRepository = subscriberRepository;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process a payment against an invoice")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProcessPaymentUseCase.ProcessPaymentResult> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {

        // The JWT 'sub' claim is the Keycloak user UUID.
        // Resolve it to the internal subscriber UUID.
        UUID keycloakUserId = UUID.fromString(jwt.getSubject());
        UUID subscriberId = subscriberRepository.findByKeycloakUserId(keycloakUserId)
                .map(s -> s.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No subscriber found for Keycloak user: " + keycloakUserId));

        var command = new ProcessPaymentUseCase.ProcessPaymentCommand(
                request.invoiceId(),
                subscriberId,
                request.amount(),
                PaymentMethod.valueOf(request.paymentMethod()),
                request.mobileMoneyProvider(),
                request.phoneNumber()
        );

        var result = processPaymentUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get payment history for the authenticated customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyPayments() {
        // TODO: Implement get payment history
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping(value = "/methods", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get available payment methods")
    public ResponseEntity<?> getPaymentMethods() {
        return ResponseEntity.ok(java.util.Map.of(
                "methods", new String[]{
                        "MOBILE_MONEY", "CARD", "BANK_TRANSFER", "CASH"
                },
                "mobileMoneyProviders", new String[]{
                        "MVola", "Orange Money", "Airtel Money"
                }
        ));
    }
}
