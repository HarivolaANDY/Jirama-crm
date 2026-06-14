package com.jirama.infrastructure.persistence.repository;

import com.jirama.domain.incident.Incident;
import com.jirama.domain.incident.IncidentRepository;
import com.jirama.domain.incident.enums.IncidentStatus;
import com.jirama.domain.incident.enums.IncidentType;
import com.jirama.infrastructure.persistence.entity.IncidentEntity;
import com.jirama.infrastructure.persistence.mapper.IncidentMapper;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaIncidentRepository implements IncidentRepository {

    private final SpringDataIncidentRepository springRepo;
    private final IncidentMapper mapper;

    public JpaIncidentRepository(SpringDataIncidentRepository springRepo,
                                  IncidentMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public Optional<Incident> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Incident> findByIncidentNumber(String incidentNumber) {
        return springRepo.findByIncidentNumber(incidentNumber).map(mapper::toDomain);
    }

    @Override
    public List<Incident> findBySubscriberId(UUID subscriberId) {
        return springRepo.findBySubscriberId(subscriberId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByStatus(IncidentStatus status) {
        return springRepo.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByAssignedTeamId(UUID teamId) {
        return springRepo.findByAssignedTeamId(teamId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByRegionId(UUID regionId) {
        return springRepo.findByRegionId(regionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findByType(IncidentType type) {
        return springRepo.findByIncidentType(type.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Incident> findAll(int page, int size) {
        return springRepo.findAll(PageRequest.of(page, size)).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public long countByStatus(IncidentStatus status) {
        return springRepo.countByStatus(status.name());
    }

    @Override
    public Incident save(Incident incident) {
        var entity = mapper.toEntity(incident);
        var saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}

