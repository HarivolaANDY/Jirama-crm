package com.jirama.application.auth;

import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.domain.subscriber.enums.SubscriberStatus;
import com.jirama.interfaces.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Full integration test for RegisterSubscriberUseCase.
 * Uses TestContainers to spin up a real PostgreSQL database.
 * Runs Flyway migrations, then exercises the full use case + repository pipeline.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RegisterSubscriberUseCase Integration")
class RegisterSubscriberUseCaseIntegrationTest {

    @Autowired
    private RegisterSubscriberUseCase useCase;

    @Autowired
    private SubscriberRepository subscriberRepository;

    private UUID keycloakUserId;

    @BeforeEach
    void setUp() {
        keycloakUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("should persist a new subscriber and retrieve it from the database")
    void shouldPersistNewSubscriber() {
        // Given
        var command = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean", "Rakoto", "jean.rakoto@email.com", "+261341234567",
                "Lot IVK 123", "Escalier C", "Antananarivo",
                "Ambohimanarina", "AN", keycloakUserId
        );

        // When
        RegisterSubscriberUseCase.RegisterResult result = useCase.execute(command);

        // Then — verify result
        assertThat(result.fullName()).isEqualTo("Jean Rakoto");
        assertThat(result.subscriberNumber()).startsWith("JRM-");

        // Then — verify persistence
        Optional<Subscriber> fetched = subscriberRepository.findById(UUID.fromString(result.id()));
        assertThat(fetched).isPresent();
        Subscriber subscriber = fetched.get();

        assertThat(subscriber.getFirstName()).isEqualTo("Jean");
        assertThat(subscriber.getLastName()).isEqualTo("Rakoto");
        assertThat(subscriber.getEmail()).isEqualTo("jean.rakoto@email.com");
        assertThat(subscriber.getPhoneNumber()).isEqualTo("+261341234567");
        assertThat(subscriber.getKeycloakUserId()).isEqualTo(keycloakUserId);
        assertThat(subscriber.getStatus()).isEqualTo(SubscriberStatus.ACTIVE);
        assertThat(subscriber.getSubscriberNumber()).isEqualTo(result.subscriberNumber());

        // Verify address persistence
        assertThat(subscriber.getAddress().getLine1()).isEqualTo("Lot IVK 123");
        assertThat(subscriber.getAddress().getLine2()).isEqualTo("Escalier C");
        assertThat(subscriber.getAddress().getCity()).isEqualTo("Antananarivo");
        assertThat(subscriber.getAddress().getDistrict()).isEqualTo("Ambohimanarina");
        assertThat(subscriber.getAddress().getRegionCode()).isEqualTo("AN");
    }

    @Test
    @DisplayName("should reject duplicate Keycloak user registration")
    void shouldRejectDuplicateKeycloakUser() {
        // Given — register once
        var command = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean", "Rakoto", "jean@email.com", "+261341234567",
                "Lot IVK 123", null, "Antananarivo", null, null, keycloakUserId
        );
        useCase.execute(command);

        // When — register again with same Keycloak user
        var duplicateCommand = new RegisterSubscriberUseCase.RegisterCommand(
                "Pierre", "Randria", "pierre@email.com", "+261349876543",
                "Lot ABC 456", null, "Antananarivo", null, null, keycloakUserId
        );

        // Then
        assertThatThrownBy(() -> useCase.execute(duplicateCommand))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already linked to this Keycloak user");
    }

    @Test
    @DisplayName("should reject duplicate active phone number")
    void shouldRejectDuplicateActivePhoneNumber() {
        // Given — register with a phone number
        var command = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean", "Rakoto", "jean@email.com", "+261341234567",
                "Lot IVK 123", null, "Antananarivo", null, null, UUID.randomUUID()
        );
        useCase.execute(command);

        // When — register again with same phone number under different Keycloak user
        var duplicateCommand = new RegisterSubscriberUseCase.RegisterCommand(
                "Pierre", "Randria", "pierre@email.com", "+261341234567",
                "Lot ABC 456", null, "Antananarivo", null, null, UUID.randomUUID()
        );

        // Then
        assertThatThrownBy(() -> useCase.execute(duplicateCommand))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("phone number already exists");
    }

    @Test
    @DisplayName("should find subscriber by Keycloak user ID after registration")
    void shouldFindByKeycloakUserIdAfterRegistration() {
        // Given
        var command = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean", "Rakoto", "jean@email.com", "+261341234567",
                "Lot IVK 123", null, "Antananarivo", null, null, keycloakUserId
        );

        // When
        RegisterSubscriberUseCase.RegisterResult result = useCase.execute(command);

        // Then
        Optional<Subscriber> byKeycloakUser = subscriberRepository.findByKeycloakUserId(keycloakUserId);
        assertThat(byKeycloakUser).isPresent();
        assertThat(byKeycloakUser.get().getId().toString()).isEqualTo(result.id());
    }

    @Test
    @DisplayName("should generate unique subscriber numbers for sequential registrations")
    void shouldGenerateUniqueSubscriberNumbers() {
        // Given
        var cmd1 = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean", "Rakoto", "jean1@email.com", "+261341234561",
                "Addr 1", null, "City", null, null, UUID.randomUUID()
        );
        var cmd2 = new RegisterSubscriberUseCase.RegisterCommand(
                "Pierre", "Randria", "pierre2@email.com", "+261341234562",
                "Addr 2", null, "City", null, null, UUID.randomUUID()
        );

        // When
        RegisterSubscriberUseCase.RegisterResult result1 = useCase.execute(cmd1);
        RegisterSubscriberUseCase.RegisterResult result2 = useCase.execute(cmd2);

        // Then
        assertThat(result1.subscriberNumber()).isNotEqualTo(result2.subscriberNumber());
        assertThat(result1.id()).isNotEqualTo(result2.id());
    }
}
