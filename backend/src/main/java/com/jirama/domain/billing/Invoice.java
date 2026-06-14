package com.jirama.domain.billing;

import com.jirama.domain.billing.enums.InvoiceStatus;
import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.shared.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root for billing. Represents a monthly invoice/bill
 * generated for a subscriber's service contract.
 */
public class Invoice extends BaseEntity {

    private final String invoiceNumber;
    private final UUID contractId;
    private final UUID subscriberId;
    private final LocalDate billingPeriodStart;
    private final LocalDate billingPeriodEnd;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;

    // Line items
    private Money subscriptionFee;
    private Money energyFee;
    private Money waterFee;
    private Money taxes;
    private Money penalties;
    private Money otherFees;
    private final Money totalAmount;
    private Money amountPaid;
    private final Money balanceDue;

    private BigDecimal consumptionKwh;
    private BigDecimal consumptionM3;
    private String pdfPath;
    private String tariffCode;
    private BigDecimal meterReadingStart;
    private BigDecimal meterReadingEnd;
    private String notes;

    public Invoice(UUID id, Instant createdAt, Instant updatedAt, long version,
                   String invoiceNumber, UUID contractId, UUID subscriberId,
                   LocalDate billingPeriodStart, LocalDate billingPeriodEnd,
                   LocalDate issueDate, LocalDate dueDate, InvoiceStatus status,
                   Money subscriptionFee, Money energyFee, Money waterFee,
                   Money taxes, Money penalties, Money otherFees,
                   Money totalAmount, Money amountPaid, Money balanceDue,
                   BigDecimal consumptionKwh, BigDecimal consumptionM3,
                   String pdfPath, String tariffCode,
                   BigDecimal meterReadingStart, BigDecimal meterReadingEnd,
                   String notes) {
        super(id, createdAt, updatedAt, version);
        this.invoiceNumber = invoiceNumber;
        this.contractId = contractId;
        this.subscriberId = subscriberId;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.subscriptionFee = subscriptionFee;
        this.energyFee = energyFee;
        this.waterFee = waterFee;
        this.taxes = taxes;
        this.penalties = penalties;
        this.otherFees = otherFees;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid;
        this.balanceDue = balanceDue;
        this.consumptionKwh = consumptionKwh;
        this.consumptionM3 = consumptionM3;
        this.pdfPath = pdfPath;
        this.tariffCode = tariffCode;
        this.meterReadingStart = meterReadingStart;
        this.meterReadingEnd = meterReadingEnd;
        this.notes = notes;
    }

    public static Invoice create(String invoiceNumber, UUID contractId, UUID subscriberId,
                                  LocalDate billingPeriodStart, LocalDate billingPeriodEnd,
                                  LocalDate dueDate, Money totalAmount,
                                  BigDecimal consumptionKwh, String tariffCode) {
        return new Invoice(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                invoiceNumber, contractId, subscriberId,
                billingPeriodStart, billingPeriodEnd,
                LocalDate.now(), dueDate, InvoiceStatus.PENDING,
                Money.ZERO, Money.ZERO, Money.ZERO,
                Money.ZERO, Money.ZERO, Money.ZERO,
                totalAmount, Money.ZERO, totalAmount,
                consumptionKwh, null, null, tariffCode,
                null, null, null
        );
    }

    public void markAsPaid(Money paymentAmount) {
        if (this.status == InvoiceStatus.PAID || this.status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already " + this.status);
        }
        this.amountPaid = this.amountPaid.add(paymentAmount);
        if (!this.amountPaid.isLessThan(this.totalAmount)) {
            this.status = InvoiceStatus.PAID;
        } else {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        }
        markUpdated();
    }

    public void markAsOverdue() {
        if (this.status != InvoiceStatus.PENDING) {
            throw new IllegalStateException("Only pending invoices can become overdue");
        }
        this.status = InvoiceStatus.OVERDUE;
        markUpdated();
    }

    public void cancel(String reason) {
        if (this.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel a paid invoice");
        }
        this.status = InvoiceStatus.CANCELLED;
        this.notes = reason;
        markUpdated();
    }

    public boolean isOverdue() {
        return this.dueDate.isBefore(LocalDate.now())
                && (this.status == InvoiceStatus.PENDING || this.status == InvoiceStatus.OVERDUE);
    }

    public Money getRemainingAmount() {
        return this.totalAmount.subtract(this.amountPaid);
    }

    // Getters
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getContractId() { return contractId; }
    public UUID getSubscriberId() { return subscriberId; }
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; }
    public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public InvoiceStatus getStatus() { return status; }
    public Money getSubscriptionFee() { return subscriptionFee; }
    public Money getEnergyFee() { return energyFee; }
    public Money getWaterFee() { return waterFee; }
    public Money getTaxes() { return taxes; }
    public Money getPenalties() { return penalties; }
    public Money getOtherFees() { return otherFees; }
    public Money getTotalAmount() { return totalAmount; }
    public Money getAmountPaid() { return amountPaid; }
    public Money getBalanceDue() { return balanceDue; }
    public BigDecimal getConsumptionKwh() { return consumptionKwh; }
    public BigDecimal getConsumptionM3() { return consumptionM3; }
    public String getPdfPath() { return pdfPath; }
    public String getTariffCode() { return tariffCode; }
    public BigDecimal getMeterReadingStart() { return meterReadingStart; }
    public BigDecimal getMeterReadingEnd() { return meterReadingEnd; }
    public String getNotes() { return notes; }
}
