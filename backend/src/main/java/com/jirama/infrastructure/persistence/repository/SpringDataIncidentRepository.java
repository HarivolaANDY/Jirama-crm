package com.jirama.infrastructure.persistence.repository;

import com.jirama.infrastructure.persistence.entity.IncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for IncidentEntity.
 */
public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Optional<IncidentEntity> findByIncidentNumber(String incidentNumber);

    List<IncidentEntity> findBySubscriberId(UUID subscriberId);

    List<IncidentEntity> findByStatus(String status);

    List<IncidentEntity> findByAssignedTeamId(UUID teamId);

    List<IncidentEntity> findByRegionId(UUID regionId);

    List<IncidentEntity> findByIncidentType(String incidentType);

    long countByStatus(String status);
}
