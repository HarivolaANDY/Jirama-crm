package com.jirama.application.incident;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.incident.Incident;
import com.jirama.domain.incident.IncidentRepository;
import com.jirama.domain.incident.enums.IncidentSeverity;
import com.jirama.domain.incident.enums.IncidentType;

import java.util.UUID;

/**
 * Use case for reporting a new service incident.
 */
@UseCase
public class ReportIncidentUseCase {

    private final IncidentRepository incidentRepository;

    public ReportIncidentUseCase(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public record ReportIncidentCommand(
            UUID subscriberId,
            IncidentType incidentType,
            IncidentSeverity severity,
            String description,
            Double locationLat,
            Double locationLng,
            String address,
            UUID regionId,
            UUID reportedBy
    ) {}

    public record ReportIncidentResult(
            String id,
            String incidentNumber,
            String status,
            String message
    ) {}

    public ReportIncidentResult execute(ReportIncidentCommand command) {
        String incidentNumber = generateIncidentNumber();

        Incident incident = Incident.report(
                incidentNumber, command.subscriberId(), command.incidentType(),
                command.severity(), command.description(), command.locationLat(),
                command.locationLng(), command.address(), command.regionId(),
                command.reportedBy()
        );

        Incident saved = incidentRepository.save(incident);

        return new ReportIncidentResult(
                saved.getId().toString(),
                saved.getIncidentNumber(),
                saved.getStatus().name(),
                "Incident reported successfully. Reference: " + saved.getIncidentNumber()
        );
    }

    private String generateIncidentNumber() {
        long count = incidentRepository.count() + 1;
        return String.format("INC-%d-%06d", java.time.Year.now().getValue(), count);
    }
}
