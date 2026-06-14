package com.jirama.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_invoice_number", columnList = "invoiceNumber", unique = true),
    @Index(name = "idx_invoice_subscriber", columnList = "subscriberId"),
    @Index(name = "idx_invoice_contract", columnList = "contractId"),
    @Index(name = "idx_invoice_status", columnList = "status"),
    @Index(name = "idx_invoice_due_date", columnList = "dueDate")
})
public class InvoiceEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @Column(nullable = false)
    private UUID contractId;

    @Column(nullable = false)
    private UUID subscriberId;

    @Column(nullable = false)
    private LocalDate billingPeriodStart;

    @Column(nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subscriptionFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal energyFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal waterFee;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal penalties;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal otherFees;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDue;

    @Column(precision = 12, scale = 2)
    private BigDecimal consumptionKwh;

    @Column(precision = 12, scale = 2)
    private BigDecimal consumptionM3;

    @Column(length = 500)
    private String pdfPath;

    @Column(length = 20)
    private String tariffCode;

    @Column(precision = 12, scale = 2)
    private BigDecimal meterReadingStart;

    @Column(precision = 12, scale = 2)
    private BigDecimal meterReadingEnd;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public InvoiceEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public UUID getContractId() { return contractId; }
    public void setContractId(UUID contractId) { this.contractId = contractId; }
    public UUID getSubscriberId() { return subscriberId; }
    public void setSubscriberId(UUID subscriberId) { this.subscriberId = subscriberId; }
    public LocalDate getBillingPeriodStart() { return billingPeriodStart; }
    public void setBillingPeriodStart(LocalDate billingPeriodStart) { this.billingPeriodStart = billingPeriodStart; }
    public LocalDate getBillingPeriodEnd() { return billingPeriodEnd; }
    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) { this.billingPeriodEnd = billingPeriodEnd; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getSubscriptionFee() { return subscriptionFee; }
    public void setSubscriptionFee(BigDecimal subscriptionFee) { this.subscriptionFee = subscriptionFee; }
    public BigDecimal getEnergyFee() { return energyFee; }
    public void setEnergyFee(BigDecimal energyFee) { this.energyFee = energyFee; }
    public BigDecimal getWaterFee() { return waterFee; }
    public void setWaterFee(BigDecimal waterFee) { this.waterFee = waterFee; }
    public BigDecimal getTaxes() { return taxes; }
    public void setTaxes(BigDecimal taxes) { this.taxes = taxes; }
    public BigDecimal getPenalties() { return penalties; }
    public void setPenalties(BigDecimal penalties) { this.penalties = penalties; }
    public BigDecimal getOtherFees() { return otherFees; }
    public void setOtherFees(BigDecimal otherFees) { this.otherFees = otherFees; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue; }
    public BigDecimal getConsumptionKwh() { return consumptionKwh; }
    public void setConsumptionKwh(BigDecimal consumptionKwh) { this.consumptionKwh = consumptionKwh; }
    public BigDecimal getConsumptionM3() { return consumptionM3; }
    public void setConsumptionM3(BigDecimal consumptionM3) { this.consumptionM3 = consumptionM3; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
    public String getTariffCode() { return tariffCode; }
    public void setTariffCode(String tariffCode) { this.tariffCode = tariffCode; }
    public BigDecimal getMeterReadingStart() { return meterReadingStart; }
    public void setMeterReadingStart(BigDecimal meterReadingStart) { this.meterReadingStart = meterReadingStart; }
    public BigDecimal getMeterReadingEnd() { return meterReadingEnd; }
    public void setMeterReadingEnd(BigDecimal meterReadingEnd) { this.meterReadingEnd = meterReadingEnd; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
