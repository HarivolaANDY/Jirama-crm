package com.jirama.infrastructure.persistence.repository;

import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.InvoiceRepository;
import com.jirama.domain.billing.enums.InvoiceStatus;
import com.jirama.infrastructure.persistence.entity.InvoiceEntity;
import com.jirama.infrastructure.persistence.mapper.InvoiceMapper;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaInvoiceRepository implements InvoiceRepository {

    private final SpringDataInvoiceRepository springRepo;
    private final InvoiceMapper mapper;

    public JpaInvoiceRepository(SpringDataInvoiceRepository springRepo, InvoiceMapper mapper) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return springRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return springRepo.findByInvoiceNumber(invoiceNumber).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findBySubscriberId(UUID subscriberId) {
        return springRepo.findBySubscriberId(subscriberId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByContractId(UUID contractId) {
        return springRepo.findByContractId(contractId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findBySubscriberIdAndStatus(UUID subscriberId, InvoiceStatus status) {
        return springRepo.findBySubscriberIdAndStatus(subscriberId, status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findOverdueInvoices(LocalDate date) {
        return springRepo.findOverdueInvoices(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByBillingPeriod(LocalDate start, LocalDate end) {
        return springRepo.findByBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(start, end)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return springRepo.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springRepo.count();
    }

    @Override
    public long countByStatus(InvoiceStatus status) {
        return springRepo.countByStatus(status.name());
    }

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = mapper.toEntity(invoice);
        InvoiceEntity saved = springRepo.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}

