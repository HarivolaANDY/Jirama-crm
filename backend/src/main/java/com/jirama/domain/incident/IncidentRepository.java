package com.jirama.domain.incident;

import com.jirama.domain.incident.enums.IncidentStatus;
import com.jirama.domain.incident.enums.IncidentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Incident persistence.
 */
public interface IncidentRepository {

    Optional<Incident> findById(UUID id);

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    List<Incident> findBySubscriberId(UUID subscriberId);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findByAssignedTeamId(UUID teamId);

    List<Incident> findByRegionId(UUID regionId);

    List<Incident> findByType(IncidentType type);

    List<Incident> findAll(int page, int size);

    long count();

    long countByStatus(IncidentStatus status);

    Incident save(Incident incident);

    void deleteById(UUID id);
}
