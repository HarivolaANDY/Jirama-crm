package com.jirama.application.billing;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.InvoiceRepository;
import com.jirama.domain.billing.enums.InvoiceStatus;
import com.jirama.domain.shared.Money;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregates customer dashboard KPI data from multiple domain sources.
 */
@UseCase
public class CustomerDashboardUseCase {

    private final InvoiceRepository invoiceRepository;

    public CustomerDashboardUseCase(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public record DashboardData(
            CurrentBill currentBill,
            List<InvoiceSummary> recentInvoices,
            ConsumptionSummary consumption,
            int openIncidents
    ) {}

    public record CurrentBill(
            UUID id,
            String invoiceNumber,
            BigDecimal totalAmount,
            BigDecimal balanceDue,
            LocalDate dueDate,
            boolean isOverdue
    ) {}

    public record InvoiceSummary(
            UUID id,
            String invoiceNumber,
            String periodLabel,
            BigDecimal totalAmount,
            String status
    ) {}

    public record ConsumptionSummary(
            BigDecimal currentKwh,
            BigDecimal previousKwh,
            double changePercent
    ) {}

    public DashboardData getDashboard(UUID subscriberId, int openIncidentCount) {
        List<Invoice> allInvoices = invoiceRepository.findBySubscriberId(subscriberId);

        // Current bill — the most recent PENDING or OVERDUE invoice
        Invoice currentInvoice = allInvoices.stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PENDING
                        || i.getStatus() == InvoiceStatus.OVERDUE
                        || i.getStatus() == InvoiceStatus.PARTIALLY_PAID)
                .findFirst()
                .orElse(null);

        // Three most recent invoices
        List<Invoice> recent = allInvoices.stream()
                .sorted((a, b) -> b.getBillingPeriodEnd().compareTo(a.getBillingPeriodEnd()))
                .limit(3)
                .toList();

        // Consumption comparison (last two readings)
        ConsumptionSummary consumption = computeConsumption(allInvoices);

        CurrentBill bill = currentInvoice != null
                ? new CurrentBill(
                        currentInvoice.getId(),
                        currentInvoice.getInvoiceNumber(),
                        currentInvoice.getTotalAmount().getAmount(),
                        currentInvoice.getRemainingAmount().getAmount(),
                        currentInvoice.getDueDate(),
                        currentInvoice.isOverdue())
                : new CurrentBill(null, null, BigDecimal.ZERO, BigDecimal.ZERO, null, false);

        List<InvoiceSummary> summaries = recent.stream()
                .map(inv -> new InvoiceSummary(
                        inv.getId(),
                        inv.getInvoiceNumber(),
                        inv.getBillingPeriodStart().getMonth().name() + " "
                                + inv.getBillingPeriodStart().getYear(),
                        inv.getTotalAmount().getAmount(),
                        inv.getStatus().name()))
                .toList();

        return new DashboardData(bill, summaries, consumption, openIncidentCount);
    }

    private ConsumptionSummary computeConsumption(List<Invoice> invoices) {
        List<Invoice> withReading = invoices.stream()
                .filter(i -> i.getConsumptionKwh() != null)
                .sorted((a, b) -> b.getBillingPeriodEnd().compareTo(a.getBillingPeriodEnd()))
                .toList();

        BigDecimal current = withReading.size() > 0
                ? withReading.get(0).getConsumptionKwh() : BigDecimal.ZERO;
        BigDecimal previous = withReading.size() > 1
                ? withReading.get(1).getConsumptionKwh() : BigDecimal.ZERO;

        double change = previous.compareTo(BigDecimal.ZERO) > 0
                ? current.subtract(previous).doubleValue() / previous.doubleValue() * 100.0
                : 0.0;

        return new ConsumptionSummary(current, previous, change);
    }
}
