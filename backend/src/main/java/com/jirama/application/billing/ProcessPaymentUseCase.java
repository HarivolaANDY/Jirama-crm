package com.jirama.application.billing;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.InvoiceRepository;
import com.jirama.domain.billing.Payment;
import com.jirama.domain.billing.enums.PaymentMethod;
import com.jirama.domain.shared.Money;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case for processing a payment against an invoice.
 */
@UseCase
public class ProcessPaymentUseCase {

    private final InvoiceRepository invoiceRepository;

    public ProcessPaymentUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public record ProcessPaymentCommand(
            UUID invoiceId,
            UUID subscriberId,
            double amount,
            PaymentMethod paymentMethod,
            String mobileMoneyProvider,
            String phoneNumber
    ) {}

    public record ProcessPaymentResult(
            String paymentId,
            String paymentNumber,
            String status,
            String message
    ) {}

    public ProcessPaymentResult execute(ProcessPaymentCommand command) {
        Invoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.invoiceId()));

        if (invoice.getStatus() == com.jirama.domain.billing.enums.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }

        Money paymentAmount = new Money(BigDecimal.valueOf(command.amount()));

        if (paymentAmount.isGreaterThan(invoice.getRemainingAmount())) {
            throw new IllegalArgumentException("Payment amount exceeds remaining balance");
        }

        // Create payment
        String paymentNumber = generatePaymentNumber();
        Payment payment = Payment.create(
                paymentNumber, command.invoiceId(), command.subscriberId(),
                paymentAmount, command.paymentMethod()
        );

        // Process payment (simplified — in production, this integrates with MVola/Orange APIs)
        payment.markCompleted("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "RCP-" + paymentNumber);

        // Update invoice
        invoice.markAsPaid(paymentAmount);
        invoiceRepository.save(invoice);

        return new ProcessPaymentResult(
                payment.getId().toString(),
                payment.getPaymentNumber(),
                payment.getStatus().name(),
                "Payment processed successfully"
        );
    }

    private String generatePaymentNumber() {
        long timestamp = System.currentTimeMillis();
        return String.format("PAY-%d-%d", java.time.Year.now().getValue(), timestamp % 1000000);
    }
}
