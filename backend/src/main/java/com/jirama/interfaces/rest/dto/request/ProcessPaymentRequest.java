package com.jirama.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull(message = "Invoice ID is required")
        UUID invoiceId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Double amount,

        @NotBlank(message = "Payment method is required")
        @Size(max = 20)
        String paymentMethod,

        @Size(max = 30)
        String mobileMoneyProvider,

        @Size(max = 20)
        String phoneNumber
) {}
