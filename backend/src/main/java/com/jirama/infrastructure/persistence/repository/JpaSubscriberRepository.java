package com.jirama.infrastructure.persistence.repository;

import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.infrastructure.persistence.entity.SubscriberEntity;
import com.jirama.infrastructure.persistence.mapper.SubscriberMapper;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the SubscriberRepository port.
 * Maps between domain entities and JPA entities transparently.
 */
@Repository
public class JpaSubscriberRepository implements SubscriberRepository {

    private final SpringDataSubscriberRepository springRepo;
    private final SubscriberMapper mapper;

    public JpaSubscriberRepository(SpringDataSubscriberRepository springRepo, SubscriberMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public Optional<Subscriber> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscriber> findBySubscriberNumber(String subscriberNumber) {
        return springRepo.findBySubscriberNumber(subscriberNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscriber> findByKeycloakUserId(UUID keycloakUserId) {
        return springRepo.findByKeycloakUserId(keycloakUserId).map(mapper::toDomain);
    }

    @Override
    public List<Subscriber> findByPhoneNumber(String phoneNumber) {
        return springRepo.findByPhoneNumber(phoneNumber).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscriber> findByEmail(String email) {
        return springRepo.findByEmail(email).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscriber> search(String query, int page, int size) {
        return springRepo.searchSubscribers(query).stream()
                .map(mapper::toDomain)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public Subscriber save(Subscriber subscriber) {
        SubscriberEntity entity = mapper.toEntity(subscriber);
        SubscriberEntity saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.softDelete(id);
    }

    @Override
    public boolean existsBySubscriberNumber(String subscriberNumber) {
        return springRepo.existsBySubscriberNumber(subscriberNumber);
    }
}


