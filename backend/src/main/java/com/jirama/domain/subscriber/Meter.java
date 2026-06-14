package com.jirama.domain.subscriber;

import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.subscriber.enums.MeterStatus;
import com.jirama.domain.subscriber.enums.MeterType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a utility meter installed at a subscriber's premises.
 * Each meter belongs to a contract and records consumption readings.
 */
public class Meter extends BaseEntity {

    private final String meterNumber;
    private final UUID contractId;
    private final MeterType meterType;
    private String model;
    private String manufacturer;
    private LocalDate installationDate;
    private Instant lastReadingDate;
    private BigDecimal lastReadingValue;
    private BigDecimal initialReading;
    private BigDecimal multiplierFactor;
    private MeterStatus status;
    private Double locationLat;
    private Double locationLng;
    private String sealNumber;
    private String qrCode;
    private String notes;

    public Meter(UUID id, Instant createdAt, Instant updatedAt, long version,
                 String meterNumber, UUID contractId, MeterType meterType,
                 String model, String manufacturer, LocalDate installationDate,
                 Instant lastReadingDate, BigDecimal lastReadingValue,
                 BigDecimal initialReading, BigDecimal multiplierFactor,
                 MeterStatus status, Double locationLat, Double locationLng,
                 String sealNumber, String qrCode, String notes) {
        super(id, createdAt, updatedAt, version);
        this.meterNumber = meterNumber;
        this.contractId = contractId;
        this.meterType = meterType;
        this.model = model;
        this.manufacturer = manufacturer;
        this.installationDate = installationDate;
        this.lastReadingDate = lastReadingDate;
        this.lastReadingValue = lastReadingValue;
        this.initialReading = initialReading;
        this.multiplierFactor = multiplierFactor;
        this.status = status;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.sealNumber = sealNumber;
        this.qrCode = qrCode;
        this.notes = notes;
    }

    public static Meter create(String meterNumber, UUID contractId, MeterType meterType,
                                LocalDate installationDate, BigDecimal initialReading) {
        return new Meter(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                meterNumber, contractId, meterType, null, null,
                installationDate, null, null, initialReading,
                BigDecimal.ONE, MeterStatus.ACTIVE, null, null,
                null, null, null
        );
    }

    public void recordReading(Instant readingDate, BigDecimal readingValue) {
        if (readingValue.compareTo(this.lastReadingValue != null ? this.lastReadingValue : this.initialReading) < 0) {
            throw new IllegalArgumentException(
                    "Reading value cannot be less than previous reading");
        }
        this.lastReadingDate = readingDate;
        this.lastReadingValue = readingValue;
        markUpdated();
    }

    public void markFaulty(String notes) {
        this.status = MeterStatus.FAULTY;
        this.notes = notes;
        markUpdated();
    }

    public BigDecimal calculateConsumption(BigDecimal currentReading) {
        BigDecimal previous = this.lastReadingValue != null ? this.lastReadingValue : this.initialReading;
        return currentReading.subtract(previous).multiply(this.multiplierFactor);
    }

    // Getters
    public String getMeterNumber() { return meterNumber; }
    public UUID getContractId() { return contractId; }
    public MeterType getMeterType() { return meterType; }
    public String getModel() { return model; }
    public String getManufacturer() { return manufacturer; }
    public LocalDate getInstallationDate() { return installationDate; }
    public Instant getLastReadingDate() { return lastReadingDate; }
    public BigDecimal getLastReadingValue() { return lastReadingValue; }
    public BigDecimal getInitialReading() { return initialReading; }
    public BigDecimal getMultiplierFactor() { return multiplierFactor; }
    public MeterStatus getStatus() { return status; }
    public Double getLocationLat() { return locationLat; }
    public Double getLocationLng() { return locationLng; }
    public String getSealNumber() { return sealNumber; }
    public String getQrCode() { return qrCode; }
    public String getNotes() { return notes; }
}
