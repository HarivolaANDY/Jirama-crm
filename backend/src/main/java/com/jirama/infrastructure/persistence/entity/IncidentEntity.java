package com.jirama.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidents", indexes = {
    @Index(name = "idx_incident_number", columnList = "incidentNumber", unique = true),
    @Index(name = "idx_incident_subscriber", columnList = "subscriberId"),
    @Index(name = "idx_incident_status", columnList = "status"),
    @Index(name = "idx_incident_region", columnList = "regionId")
})
public class IncidentEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String incidentNumber;

    private UUID subscriberId;

    @Column(nullable = false, length = 30)
    private String incidentType;

    @Column(nullable = false, length = 20)
    private String severity;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal locationLat;

    @Column(precision = 10, scale = 7)
    private java.math.BigDecimal locationLng;

    @Column(length = 255)
    private String address;

    private UUID regionId;

    private Integer affectedContracts;

    private UUID assignedTeamId;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    private Instant resolvedAt;

    @Column(nullable = false)
    private UUID reportedBy;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public IncidentEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getIncidentNumber() { return incidentNumber; }
    public void setIncidentNumber(String incidentNumber) { this.incidentNumber = incidentNumber; }
    public UUID getSubscriberId() { return subscriberId; }
    public void setSubscriberId(UUID subscriberId) { this.subscriberId = subscriberId; }
    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public java.math.BigDecimal getLocationLat() { return locationLat; }
    public void setLocationLat(java.math.BigDecimal locationLat) { this.locationLat = locationLat; }
    public java.math.BigDecimal getLocationLng() { return locationLng; }
    public void setLocationLng(java.math.BigDecimal locationLng) { this.locationLng = locationLng; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public UUID getRegionId() { return regionId; }
    public void setRegionId(UUID regionId) { this.regionId = regionId; }
    public Integer getAffectedContracts() { return affectedContracts; }
    public void setAffectedContracts(Integer affectedContracts) { this.affectedContracts = affectedContracts; }
    public UUID getAssignedTeamId() { return assignedTeamId; }
    public void setAssignedTeamId(UUID assignedTeamId) { this.assignedTeamId = assignedTeamId; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public UUID getReportedBy() { return reportedBy; }
    public void setReportedBy(UUID reportedBy) { this.reportedBy = reportedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
