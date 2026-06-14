package com.jirama.infrastructure.persistence.mapper;

import com.jirama.domain.shared.Address;
import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.enums.SubscriberStatus;
import com.jirama.domain.subscriber.enums.SubscriberType;
import com.jirama.infrastructure.persistence.entity.SubscriberEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Maps between the JPA entity and domain entity for subscribers.
 * This is the only place where JPA annotations leak into the domain layer boundary.
 */
@Component
public class SubscriberMapper {

    public SubscriberEntity toEntity(Subscriber domain) {
        SubscriberEntity entity = new SubscriberEntity();
        entity.setId(domain.getId());
        entity.setSubscriberNumber(domain.getSubscriberNumber());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setEmail(domain.getEmail());
        entity.setPhoneNumber(domain.getPhoneNumber());
        entity.setSecondaryPhone(domain.getSecondaryPhone());
        entity.setIdCardNumber(domain.getIdCardNumber());
        entity.setTaxId(domain.getTaxId());

        if (domain.getAddress() != null) {
            entity.setAddressLine1(domain.getAddress().getLine1());
            entity.setAddressLine2(domain.getAddress().getLine2());
            entity.setCity(domain.getAddress().getCity());
            entity.setDistrict(domain.getAddress().getDistrict());
            entity.setRegionCode(domain.getAddress().getRegionCode());
            entity.setPostalCode(domain.getAddress().getPostalCode());
            if (domain.getAddress().getLatitude() != null) {
                entity.setLatitude(BigDecimal.valueOf(domain.getAddress().getLatitude()));
            }
            if (domain.getAddress().getLongitude() != null) {
                entity.setLongitude(BigDecimal.valueOf(domain.getAddress().getLongitude()));
            }
        }

        entity.setStatus(domain.getStatus().name());
        entity.setSubscriberType(domain.getSubscriberType().name());
        entity.setPreferredLanguage(domain.getPreferredLanguage());
        entity.setKeycloakUserId(domain.getKeycloakUserId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        return entity;
    }

    public Subscriber toDomain(SubscriberEntity entity) {
        Address address = new Address(
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getRegionCode(),
                entity.getPostalCode(),
                entity.getLatitude() != null ? entity.getLatitude().doubleValue() : null,
                entity.getLongitude() != null ? entity.getLongitude().doubleValue() : null
        );

        return new Subscriber(
                entity.getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                entity.getSubscriberNumber(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getSecondaryPhone(),
                entity.getIdCardNumber(),
                entity.getTaxId(),
                address,
                SubscriberStatus.valueOf(entity.getStatus()),
                SubscriberType.valueOf(entity.getSubscriberType()),
                entity.getPreferredLanguage(),
                entity.getKeycloakUserId()
        );
    }
}
