package com.jirama.application.subscriber;

import com.jirama.application.shared.UseCase;
import com.jirama.domain.shared.Address;
import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.domain.subscriber.enums.SubscriberType;

/**
 * Use case for creating a new subscriber.
 * Validates business rules and persists the subscriber.
 */
@UseCase
public class CreateSubscriberUseCase {

    private final SubscriberRepository subscriberRepository;

    public CreateSubscriberUseCase(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public record CreateSubscriberCommand(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String idCardNumber,
            String addressLine1,
            String addressLine2,
            String city,
            String district,
            String regionCode,
            String postalCode,
            SubscriberType subscriberType,
            String preferredLanguage
    ) {}

    public record CreateSubscriberResult(
            String id,
            String subscriberNumber,
            String fullName,
            String email
    ) {}

    public CreateSubscriberResult execute(CreateSubscriberCommand command) {
        // Validate uniqueness
        if (subscriberRepository.findByPhoneNumber(command.phoneNumber()).stream()
                .anyMatch(s -> s.getStatus() != null)) {
            throw new IllegalArgumentException("A subscriber with this phone number already exists");
        }

        // Generate subscriber number
        String subscriberNumber = generateSubscriberNumber();

        // Build address value object
        Address address = new Address(
                command.addressLine1(), command.addressLine2(),
                command.city(), command.district(), command.regionCode(),
                command.postalCode(), null, null
        );

        // Create domain entity
        Subscriber subscriber = Subscriber.create(
                subscriberNumber, command.firstName(), command.lastName(),
                command.email(), command.phoneNumber(), address,
                command.subscriberType()
        );

        // Persist
        Subscriber saved = subscriberRepository.save(subscriber);

        return new CreateSubscriberResult(
                saved.getId().toString(),
                saved.getSubscriberNumber(),
                saved.getFullName(),
                saved.getEmail()
        );
    }

    private String generateSubscriberNumber() {
        long count = subscriberRepository.count() + 1;
        return String.format("JRM-%d-%06d", java.time.Year.now().getValue(), count);
    }
}
