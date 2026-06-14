package com.jirama.application.auth;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.shared.Address;
import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.domain.subscriber.enums.SubscriberType;
import com.jirama.interfaces.rest.exception.BusinessRuleException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for registering a new customer.
 * Creates the subscriber record and links it to an existing Keycloak user.
 */
@UseCase
@Transactional(rollbackFor = Exception.class)
public class RegisterSubscriberUseCase {

    private final SubscriberRepository subscriberRepository;

    public RegisterSubscriberUseCase(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public record RegisterCommand(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String addressLine1,
            String addressLine2,
            String city,
            String district,
            String regionCode,
            UUID keycloakUserId
    ) {}

    public record RegisterResult(
            String id,
            String subscriberNumber,
            String fullName
    ) {}

    public RegisterResult execute(RegisterCommand command) {
        // Validate uniqueness
        if (subscriberRepository.findByKeycloakUserId(command.keycloakUserId()).isPresent()) {
            throw new BusinessRuleException("DUPLICATE_KEYCLOAK_USER",
                    "A subscriber is already linked to this Keycloak user");
        }

        if (subscriberRepository.findByPhoneNumber(command.phoneNumber()).stream()
                .anyMatch(s -> s.isActive())) {
            throw new BusinessRuleException("DUPLICATE_PHONE",
                    "A subscriber with this phone number already exists");
        }

        // Generate subscriber number
        String subscriberNumber = generateSubscriberNumber();

        // Build address
        Address address = new Address(
                command.addressLine1(), command.addressLine2(),
                command.city(), command.district(), command.regionCode(),
                null, null, null
        );

        // Create subscriber
        Subscriber subscriber = Subscriber.create(
                subscriberNumber,
                command.firstName(),
                command.lastName(),
                command.email(),
                command.phoneNumber(),
                address,
                SubscriberType.RESIDENTIAL
        );

        // Link to the Keycloak user who just authenticated
        subscriber.linkToKeycloak(command.keycloakUserId());

        // Persist
        Subscriber saved = subscriberRepository.save(subscriber);

        return new RegisterResult(
                saved.getId().toString(),
                saved.getSubscriberNumber(),
                saved.getFullName()
        );
    }

    private String generateSubscriberNumber() {
        long count = subscriberRepository.count() + 1;
        return String.format("JRM-%d-%06d",
                java.time.Year.now().getValue(),
                count);
    }
}
