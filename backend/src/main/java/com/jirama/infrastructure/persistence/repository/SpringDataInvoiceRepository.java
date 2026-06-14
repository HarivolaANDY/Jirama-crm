package com.jirama.infrastructure.persistence.repository;

import com.jirama.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for InvoiceEntity.
 */
public interface SpringDataInvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {

    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);

    List<InvoiceEntity> findBySubscriberId(UUID subscriberId);

    List<InvoiceEntity> findByContractId(UUID contractId);

    List<InvoiceEntity> findBySubscriberIdAndStatus(UUID subscriberId, String status);

    List<InvoiceEntity> findByStatus(String status);

    List<InvoiceEntity> findByBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
            LocalDate start, LocalDate end);

    @Query("SELECT i FROM InvoiceEntity i WHERE i.dueDate < :date AND i.status IN ('PENDING', 'PARTIALLY_PAID')")
    List<InvoiceEntity> findOverdueInvoices(@Param("date") LocalDate date);

    long countByStatus(String status);
}
