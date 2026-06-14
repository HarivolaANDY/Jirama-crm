package com.jirama.infrastructure.persistence.mapper;

import com.jirama.domain.billing.Invoice;
import com.jirama.domain.billing.enums.InvoiceStatus;
import com.jirama.domain.shared.Money;
import com.jirama.infrastructure.persistence.entity.InvoiceEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Maps between the JPA entity and domain entity for invoices.
 */
@Component
public class InvoiceMapper {

    private static final Currency MGA = Currency.getInstance("MGA");

    public InvoiceEntity toEntity(Invoice domain) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(domain.getId());
        entity.setInvoiceNumber(domain.getInvoiceNumber());
        entity.setContractId(domain.getContractId());
        entity.setSubscriberId(domain.getSubscriberId());
        entity.setBillingPeriodStart(domain.getBillingPeriodStart());
        entity.setBillingPeriodEnd(domain.getBillingPeriodEnd());
        entity.setIssueDate(domain.getIssueDate());
        entity.setDueDate(domain.getDueDate());
        entity.setStatus(domain.getStatus().name());
        entity.setSubscriptionFee(domain.getSubscriptionFee().getAmount());
        entity.setEnergyFee(domain.getEnergyFee().getAmount());
        entity.setWaterFee(domain.getWaterFee().getAmount());
        entity.setTaxes(domain.getTaxes().getAmount());
        entity.setPenalties(domain.getPenalties().getAmount());
        entity.setOtherFees(domain.getOtherFees().getAmount());
        entity.setTotalAmount(domain.getTotalAmount().getAmount());
        entity.setAmountPaid(domain.getAmountPaid().getAmount());
        entity.setBalanceDue(domain.getBalanceDue().getAmount());
        entity.setConsumptionKwh(domain.getConsumptionKwh());
        entity.setConsumptionM3(domain.getConsumptionM3());
        entity.setPdfPath(domain.getPdfPath());
        entity.setTariffCode(domain.getTariffCode());
        entity.setMeterReadingStart(domain.getMeterReadingStart());
        entity.setMeterReadingEnd(domain.getMeterReadingEnd());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());
        return entity;
    }

    public Invoice toDomain(InvoiceEntity entity) {
        return new Invoice(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                entity.getInvoiceNumber(),
                entity.getContractId(),
                entity.getSubscriberId(),
                entity.getBillingPeriodStart(),
                entity.getBillingPeriodEnd(),
                entity.getIssueDate(),
                entity.getDueDate(),
                InvoiceStatus.valueOf(entity.getStatus()),
                new Money(entity.getSubscriptionFee(), MGA),
                new Money(entity.getEnergyFee(), MGA),
                new Money(entity.getWaterFee(), MGA),
                new Money(entity.getTaxes(), MGA),
                new Money(entity.getPenalties(), MGA),
                new Money(entity.getOtherFees(), MGA),
                new Money(entity.getTotalAmount(), MGA),
                new Money(entity.getAmountPaid(), MGA),
                new Money(entity.getBalanceDue(), MGA),
                entity.getConsumptionKwh(),
                entity.getConsumptionM3(),
                entity.getPdfPath(),
                entity.getTariffCode(),
                entity.getMeterReadingStart(),
                entity.getMeterReadingEnd(),
                entity.getNotes()
        );
    }
}
