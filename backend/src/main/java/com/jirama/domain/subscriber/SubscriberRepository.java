package com.jirama.domain.subscriber;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port (repository interface) for Subscriber persistence.
 * Defined in the domain layer — implementation is in the infrastructure layer.
 */
public interface SubscriberRepository {

    Optional<Subscriber> findById(UUID id);

    Optional<Subscriber> findBySubscriberNumber(String subscriberNumber);

    Optional<Subscriber> findByKeycloakUserId(UUID keycloakUserId);

    List<Subscriber> findByPhoneNumber(String phoneNumber);

    List<Subscriber> findByEmail(String email);

    List<Subscriber> search(String query, int page, int size);

    long count();

    Subscriber save(Subscriber subscriber);

    void deleteById(UUID id);

    boolean existsBySubscriberNumber(String subscriberNumber);
}
