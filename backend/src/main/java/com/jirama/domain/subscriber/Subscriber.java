package com.jirama.domain.subscriber;

import com.jirama.domain.shared.Address;
import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.subscriber.enums.SubscriberStatus;
import com.jirama.domain.subscriber.enums.SubscriberType;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for the subscriber/customer domain.
 * Represents a citizen or entity that receives JIRAMA services.
 */
public class Subscriber extends BaseEntity {

    private final String subscriberNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String secondaryPhone;
    private String idCardNumber;
    private String taxId;
    private Address address;
    private SubscriberStatus status;
    private SubscriberType subscriberType;
    private String preferredLanguage;
    private UUID keycloakUserId;

    // Full constructor for reconstruction from persistence
    public Subscriber(UUID id, Instant createdAt, Instant updatedAt, long version,
                      String subscriberNumber, String firstName, String lastName,
                      String email, String phoneNumber, String secondaryPhone,
                      String idCardNumber, String taxId, Address address,
                      SubscriberStatus status, SubscriberType subscriberType,
                      String preferredLanguage, UUID keycloakUserId) {
        super(id, createdAt, updatedAt, version);
        this.subscriberNumber = subscriberNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.secondaryPhone = secondaryPhone;
        this.idCardNumber = idCardNumber;
        this.taxId = taxId;
        this.address = address;
        this.status = status;
        this.subscriberType = subscriberType;
        this.preferredLanguage = preferredLanguage;
        this.keycloakUserId = keycloakUserId;
    }

    // Factory method for new subscribers
    public static Subscriber create(String subscriberNumber, String firstName, String lastName,
                                    String email, String phoneNumber, Address address,
                                    SubscriberType subscriberType) {
        return new Subscriber(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                subscriberNumber, firstName, lastName, email, phoneNumber, null,
                null, null, address, SubscriberStatus.ACTIVE, subscriberType,
                "fr", null
        );
    }

    // Business methods
    public void updatePersonalInfo(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        markUpdated();
    }

    public void updateAddress(Address newAddress) {
        this.address = newAddress;
        markUpdated();
    }

    public void suspend(String reason) {
        if (this.status == SubscriberStatus.BLACKLISTED) {
            throw new IllegalStateException("Cannot suspend a blacklisted subscriber");
        }
        this.status = SubscriberStatus.SUSPENDED;
        markUpdated();
    }

    public void reactivate() {
        if (this.status == SubscriberStatus.BLACKLISTED) {
            throw new IllegalStateException("Cannot reactivate a blacklisted subscriber");
        }
        this.status = SubscriberStatus.ACTIVE;
        markUpdated();
    }

    public void linkToKeycloak(UUID keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
        markUpdated();
    }

    /** Returns true if the subscriber is currently active */
    public boolean isActive() {
        return this.status == SubscriberStatus.ACTIVE;
    }

    // Getters
    public String getSubscriberNumber() { return subscriberNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSecondaryPhone() { return secondaryPhone; }
    public String getIdCardNumber() { return idCardNumber; }
    public String getTaxId() { return taxId; }
    public Address getAddress() { return address; }
    public SubscriberStatus getStatus() { return status; }
    public SubscriberType getSubscriberType() { return subscriberType; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public UUID getKeycloakUserId() { return keycloakUserId; }
}
