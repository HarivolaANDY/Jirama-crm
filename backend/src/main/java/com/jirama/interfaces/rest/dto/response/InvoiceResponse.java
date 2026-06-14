package com.jirama.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        UUID subscriberId,
        UUID contractId,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        LocalDate issueDate,
        LocalDate dueDate,
        String status,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceDue,
        BigDecimal consumptionKwh,
        String pdfPath,
        Instant createdAt
) {}
