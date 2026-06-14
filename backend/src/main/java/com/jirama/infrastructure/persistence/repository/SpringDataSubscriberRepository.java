package com.jirama.infrastructure.persistence.repository;

import com.jirama.infrastructure.persistence.entity.SubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for SubscriberEntity.
 * Must be in its own file (not nested) for Spring Data to discover and proxy it.
 */
public interface SpringDataSubscriberRepository extends JpaRepository<SubscriberEntity, UUID> {

    Optional<SubscriberEntity> findBySubscriberNumber(String subscriberNumber);

    Optional<SubscriberEntity> findByKeycloakUserId(UUID keycloakUserId);

    List<SubscriberEntity> findByPhoneNumber(String phoneNumber);

    List<SubscriberEntity> findByEmail(String email);

    boolean existsBySubscriberNumber(String subscriberNumber);

    @Query("SELECT s FROM SubscriberEntity s WHERE s.deletedAt IS NULL AND " +
           "(LOWER(s.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.subscriberNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<SubscriberEntity> searchSubscribers(@Param("query") String query);

    @Modifying
    @Transactional
    @Query("UPDATE SubscriberEntity s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id")
    void softDelete(@Param("id") UUID id);
}
