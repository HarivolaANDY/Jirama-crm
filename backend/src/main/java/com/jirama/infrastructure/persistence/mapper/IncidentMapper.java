package com.jirama.infrastructure.persistence.mapper;

import com.jirama.domain.incident.Incident;
import com.jirama.domain.incident.enums.IncidentSeverity;
import com.jirama.domain.incident.enums.IncidentStatus;
import com.jirama.domain.incident.enums.IncidentType;
import com.jirama.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IncidentMapper {

    public IncidentEntity toEntity(Incident domain) {
        IncidentEntity entity = new IncidentEntity();
        entity.setId(domain.getId());
        entity.setIncidentNumber(domain.getIncidentNumber());
        entity.setSubscriberId(domain.getSubscriberId());
        entity.setIncidentType(domain.getIncidentType().name());
        entity.setSeverity(domain.getSeverity().name());
        entity.setStatus(domain.getStatus().name());
        entity.setDescription(domain.getDescription());
        if (domain.getLocationLat() != null) entity.setLocationLat(BigDecimal.valueOf(domain.getLocationLat()));
        if (domain.getLocationLng() != null) entity.setLocationLng(BigDecimal.valueOf(domain.getLocationLng()));
        entity.setAddress(domain.getAddress());
        entity.setRegionId(domain.getRegionId());
        entity.setAffectedContracts(domain.getAffectedContracts());
        entity.setAssignedTeamId(domain.getAssignedTeamId());
        entity.setResolutionNotes(domain.getResolutionNotes());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setReportedBy(domain.getReportedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    public Incident toDomain(IncidentEntity entity) {
        return new Incident(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                entity.getIncidentNumber(),
                entity.getSubscriberId(),
                IncidentType.valueOf(entity.getIncidentType()),
                IncidentSeverity.valueOf(entity.getSeverity()),
                IncidentStatus.valueOf(entity.getStatus()),
                entity.getDescription(),
                entity.getLocationLat() != null ? entity.getLocationLat().doubleValue() : null,
                entity.getLocationLng() != null ? entity.getLocationLng().doubleValue() : null,
                entity.getAddress(),
                entity.getRegionId(),
                entity.getAffectedContracts(),
                entity.getAssignedTeamId(),
                entity.getResolutionNotes(),
                entity.getResolvedAt(),
                entity.getReportedBy()
        );
    }
}
