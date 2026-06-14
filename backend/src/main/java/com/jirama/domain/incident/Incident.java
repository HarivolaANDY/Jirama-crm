package com.jirama.domain.incident;

import com.jirama.domain.incident.enums.IncidentSeverity;
import com.jirama.domain.incident.enums.IncidentStatus;
import com.jirama.domain.incident.enums.IncidentType;
import com.jirama.domain.shared.BaseEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a service incident reported by a customer, agent, or automatically.
 */
public class Incident extends BaseEntity {

    private final String incidentNumber;
    private final UUID subscriberId;
    private final IncidentType incidentType;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String description;
    private Double locationLat;
    private Double locationLng;
    private String address;
    private UUID regionId;
    private Integer affectedContracts;
    private UUID assignedTeamId;
    private String resolutionNotes;
    private Instant resolvedAt;
    private final UUID reportedBy;

    public Incident(UUID id, Instant createdAt, Instant updatedAt, long version,
                    String incidentNumber, UUID subscriberId,
                    IncidentType incidentType, IncidentSeverity severity,
                    IncidentStatus status, String description,
                    Double locationLat, Double locationLng, String address,
                    UUID regionId, Integer affectedContracts,
                    UUID assignedTeamId, String resolutionNotes,
                    Instant resolvedAt, UUID reportedBy) {
        super(id, createdAt, updatedAt, version);
        this.incidentNumber = incidentNumber;
        this.subscriberId = subscriberId;
        this.incidentType = incidentType;
        this.severity = severity;
        this.status = status;
        this.description = description;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.address = address;
        this.regionId = regionId;
        this.affectedContracts = affectedContracts;
        this.assignedTeamId = assignedTeamId;
        this.resolutionNotes = resolutionNotes;
        this.resolvedAt = resolvedAt;
        this.reportedBy = reportedBy;
    }

    public static Incident report(String incidentNumber, UUID subscriberId,
                                   IncidentType type, IncidentSeverity severity,
                                   String description, Double lat, Double lng,
                                   String address, UUID regionId, UUID reportedBy) {
        return new Incident(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                incidentNumber, subscriberId, type, severity,
                IncidentStatus.REPORTED, description, lat, lng,
                address, regionId, null, null, null, null, reportedBy
        );
    }

    public void confirm() {
        if (this.status != IncidentStatus.REPORTED) {
            throw new IllegalStateException("Only reported incidents can be confirmed");
        }
        this.status = IncidentStatus.CONFIRMED;
        markUpdated();
    }

    public void assignToTeam(UUID teamId) {
        if (this.status != IncidentStatus.CONFIRMED && this.status != IncidentStatus.REPORTED) {
            throw new IllegalStateException("Incident must be confirmed before assignment");
        }
        this.assignedTeamId = teamId;
        this.status = IncidentStatus.ASSIGNED;
        markUpdated();
    }

    public void startProgress() {
        if (this.status != IncidentStatus.ASSIGNED) {
            throw new IllegalStateException("Only assigned incidents can be started");
        }
        this.status = IncidentStatus.IN_PROGRESS;
        markUpdated();
    }

    public void resolve(String notes) {
        if (this.status != IncidentStatus.IN_PROGRESS && this.status != IncidentStatus.ASSIGNED) {
            throw new IllegalStateException("Incident must be in progress to resolve");
        }
        this.resolutionNotes = notes;
        this.resolvedAt = Instant.now();
        this.status = IncidentStatus.RESOLVED;
        markUpdated();
    }

    public void close() {
        if (this.status != IncidentStatus.RESOLVED) {
            throw new IllegalStateException("Only resolved incidents can be closed");
        }
        this.status = IncidentStatus.CLOSED;
        markUpdated();
    }

    // Getters
    public String getIncidentNumber() { return incidentNumber; }
    public UUID getSubscriberId() { return subscriberId; }
    public IncidentType getIncidentType() { return incidentType; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public Double getLocationLat() { return locationLat; }
    public Double getLocationLng() { return locationLng; }
    public String getAddress() { return address; }
    public UUID getRegionId() { return regionId; }
    public Integer getAffectedContracts() { return affectedContracts; }
    public UUID getAssignedTeamId() { return assignedTeamId; }
    public String getResolutionNotes() { return resolutionNotes; }
    public Instant getResolvedAt() { return resolvedAt; }
    public UUID getReportedBy() { return reportedBy; }
}
