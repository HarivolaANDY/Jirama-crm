package com.jirama.domain.subscriber;

import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.subscriber.enums.ContractStatus;
import com.jirama.domain.subscriber.enums.ContractType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a service contract between JIRAMA and a subscriber.
 * A subscriber can have multiple contracts (electricity, water, or both).
 */
public class Contract extends BaseEntity {

    private final String contractNumber;
    private final UUID subscriberId;
    private final ContractType contractType;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String terminationReason;
    private BigDecimal connectionPower;      // kVA for electricity
    private BigDecimal waterFlowRate;        // m³/h for water
    private String tariffCode;
    private String billingCycle;
    private BigDecimal depositAmount;
    private String notes;

    public Contract(UUID id, Instant createdAt, Instant updatedAt, long version,
                    String contractNumber, UUID subscriberId, ContractType contractType,
                    ContractStatus status, LocalDate startDate, LocalDate endDate,
                    String terminationReason, BigDecimal connectionPower,
                    BigDecimal waterFlowRate, String tariffCode, String billingCycle,
                    BigDecimal depositAmount, String notes) {
        super(id, createdAt, updatedAt, version);
        this.contractNumber = contractNumber;
        this.subscriberId = subscriberId;
        this.contractType = contractType;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.terminationReason = terminationReason;
        this.connectionPower = connectionPower;
        this.waterFlowRate = waterFlowRate;
        this.tariffCode = tariffCode;
        this.billingCycle = billingCycle;
        this.depositAmount = depositAmount;
        this.notes = notes;
    }

    public static Contract create(String contractNumber, UUID subscriberId,
                                   ContractType contractType, String tariffCode,
                                   LocalDate startDate, BigDecimal connectionPower) {
        return new Contract(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                contractNumber, subscriberId, contractType, ContractStatus.PENDING,
                startDate, null, null, connectionPower, null,
                tariffCode, "MONTHLY", null, null
        );
    }

    public void activate() {
        if (this.status != ContractStatus.PENDING) {
            throw new IllegalStateException("Only pending contracts can be activated");
        }
        this.status = ContractStatus.ACTIVE;
        markUpdated();
    }

    public void suspend(String reason) {
        if (this.status != ContractStatus.ACTIVE) {
            throw new IllegalStateException("Only active contracts can be suspended");
        }
        this.terminationReason = reason;
        this.status = ContractStatus.SUSPENDED;
        markUpdated();
    }

    public void terminate(String reason, LocalDate endDate) {
        if (this.status != ContractStatus.ACTIVE && this.status != ContractStatus.SUSPENDED) {
            throw new IllegalStateException("Contract cannot be terminated in current state");
        }
        this.terminationReason = reason;
        this.endDate = endDate;
        this.status = ContractStatus.TERMINATED;
        markUpdated();
    }

    // Getters
    public String getContractNumber() { return contractNumber; }
    public UUID getSubscriberId() { return subscriberId; }
    public ContractType getContractType() { return contractType; }
    public ContractStatus getStatus() { return status; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getTerminationReason() { return terminationReason; }
    public BigDecimal getConnectionPower() { return connectionPower; }
    public BigDecimal getWaterFlowRate() { return waterFlowRate; }
    public String getTariffCode() { return tariffCode; }
    public String getBillingCycle() { return billingCycle; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public String getNotes() { return notes; }
}
