package com.jirama.application.billing;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.InvoiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Use case for generating a monthly invoice for a contract.
 * This is typically called in batch by a scheduled job.
 */
@UseCase
public class GenerateInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;

    public GenerateInvoiceUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public record GenerateInvoiceCommand(
            String invoiceNumber,
            java.util.UUID contractId,
            java.util.UUID subscriberId,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            BigDecimal totalAmount,
            BigDecimal consumptionKwh,
            String tariffCode,
            LocalDate dueDate
    ) {}

    public record GenerateInvoiceResult(
            String id,
            String invoiceNumber,
            BigDecimal totalAmount,
            LocalDate dueDate
    ) {}

    public GenerateInvoiceResult execute(GenerateInvoiceCommand command) {
        Invoice invoice = Invoice.create(
                command.invoiceNumber(),
                command.contractId(),
                command.subscriberId(),
                command.billingPeriodStart(),
                command.billingPeriodEnd(),
                command.dueDate(),
                new com.jirama.domain.shared.Money(command.totalAmount()),
                command.consumptionKwh(),
                command.tariffCode()
        );

        Invoice saved = invoiceRepository.save(invoice);

        return new GenerateInvoiceResult(
                saved.getId().toString(),
                saved.getInvoiceNumber(),
                saved.getTotalAmount().getAmount(),
                saved.getDueDate()
        );
    }
}
