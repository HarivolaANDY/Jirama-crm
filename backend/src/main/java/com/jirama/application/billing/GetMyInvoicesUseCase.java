package com.jirama.application.billing;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.InvoiceRepository;
import com.jirama.domain.billing.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Use case for listing invoices belonging to an authenticated subscriber.
 */
@UseCase
public class GetMyInvoicesUseCase {

    private final InvoiceRepository invoiceRepository;

    public GetMyInvoicesUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public record InvoiceSummary(
            UUID id,
            String invoiceNumber,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            LocalDate issueDate,
            LocalDate dueDate,
            String status,
            BigDecimal totalAmount,
            BigDecimal amountPaid,
            BigDecimal balanceDue,
            BigDecimal consumptionKwh,
            String pdfPath
    ) {}

    public List<InvoiceSummary> getAll(UUID subscriberId) {
        return invoiceRepository.findBySubscriberId(subscriberId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public List<InvoiceSummary> getCurrent(UUID subscriberId) {
        return invoiceRepository.findBySubscriberIdAndStatus(subscriberId, InvoiceStatus.PENDING)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public List<InvoiceSummary> getByStatus(UUID subscriberId, InvoiceStatus status) {
        return invoiceRepository.findBySubscriberIdAndStatus(subscriberId, status)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private InvoiceSummary toSummary(Invoice invoice) {
        return new InvoiceSummary(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getBillingPeriodStart(),
                invoice.getBillingPeriodEnd(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getStatus().name(),
                invoice.getTotalAmount().getAmount(),
                invoice.getAmountPaid().getAmount(),
                invoice.getBalanceDue().getAmount(),
                invoice.getConsumptionKwh(),
                invoice.getPdfPath()
        );
    }
}
