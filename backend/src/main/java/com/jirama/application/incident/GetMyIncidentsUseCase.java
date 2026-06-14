package com.jirama.application.incident;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.incident.Incident;
import com.jirama.domain.incident.IncidentRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Use case for listing incidents belonging to an authenticated subscriber.
 */
@UseCase
public class GetMyIncidentsUseCase {

    private final IncidentRepository incidentRepository;

    public GetMyIncidentsUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public record IncidentSummary(
            UUID id,
            String incidentNumber,
            String incidentType,
            String severity,
            String status,
            String description,
            String address,
            Instant createdAt,
            Instant resolvedAt
    ) {}

    public List<IncidentSummary> getBySubscriberId(UUID subscriberId) {
        return incidentRepository.findBySubscriberId(subscriberId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public IncidentSummary getById(UUID id) {
        return incidentRepository.findById(id)
                .map(this::toSummary)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
    }

    private IncidentSummary toSummary(Incident incident) {
        return new IncidentSummary(
                incident.getId(),
                incident.getIncidentNumber(),
                incident.getIncidentType().name(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getDescription(),
                incident.getAddress(),
                incident.getCreatedAt(),
                incident.getResolvedAt()
        );
    }
}
