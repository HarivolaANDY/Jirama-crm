package com.jirama.domain.user;

import com.jirama.domain.shared.BaseEntity;
import com.jirama.domain.user.enums.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a JIRAMA staff member (agent, technician, admin, etc.).
 * Not used for customers — customers are represented by the Subscriber entity
 * and authenticated via Keycloak directly.
 */
public class User extends BaseEntity {

    private final String employeeNumber;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String position;
    private String department;
    private UUID regionId;
    private UserRole role;
    private final String keycloakId;
    private boolean active;

    public User(UUID id, Instant createdAt, Instant updatedAt, long version,
                String employeeNumber, String email, String firstName, String lastName,
                String phoneNumber, String position, String department,
                UUID regionId, UserRole role, String keycloakId, boolean active) {
        super(id, createdAt, updatedAt, version);
        this.employeeNumber = employeeNumber;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.position = position;
        this.department = department;
        this.regionId = regionId;
        this.role = role;
        this.keycloakId = keycloakId;
        this.active = active;
    }

    public static User create(String employeeNumber, String email, String firstName,
                               String lastName, String phoneNumber, UserRole role,
                               String keycloakId) {
        return new User(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                employeeNumber, email, firstName, lastName, phoneNumber,
                null, null, null, role, keycloakId, true
        );
    }

    public void deactivate() {
        this.active = false;
        markUpdated();
    }

    public void activate() {
        this.active = true;
        markUpdated();
    }

    public void changeRole(UserRole newRole) {
        this.role = newRole;
        markUpdated();
    }

    public void updateProfile(String email, String firstName, String lastName, String phoneNumber) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        markUpdated();
    }

    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    public boolean isAdminOrAbove() {
        return this.role == UserRole.ADMIN || this.role == UserRole.SUPER_ADMIN;
    }

    // Getters
    public String getEmployeeNumber() { return employeeNumber; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public UUID getRegionId() { return regionId; }
    public UserRole getRole() { return role; }
    public String getKeycloakId() { return keycloakId; }
    public boolean isActive() { return active; }
}
