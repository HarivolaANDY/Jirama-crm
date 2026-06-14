package com.jirama.domain.billing;

import com.jirama.domain.billing.enums.InvoiceStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for Invoice persistence.
 */
public interface InvoiceRepository {

    Optional<Invoice> findById(UUID id);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findBySubscriberId(UUID subscriberId);

    List<Invoice> findByContractId(UUID contractId);

    List<Invoice> findBySubscriberIdAndStatus(UUID subscriberId, InvoiceStatus status);

    List<Invoice> findOverdueInvoices(LocalDate date);

    List<Invoice> findByBillingPeriod(LocalDate start, LocalDate end);

    List<Invoice> findByStatus(InvoiceStatus status);

    long count();

    long countByStatus(InvoiceStatus status);

    Invoice save(Invoice invoice);

    void deleteById(UUID id);
}
