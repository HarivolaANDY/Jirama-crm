package com.jirama.application.auth;

import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.domain.subscriber.enums.SubscriberStatus;
import com.jirama.interfaces.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterSubscriberUseCase")
class RegisterSubscriberUseCaseTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Captor
    private ArgumentCaptor<Subscriber> subscriberCaptor;

    private RegisterSubscriberUseCase useCase;

    private UUID keycloakUserId;
    private RegisterSubscriberUseCase.RegisterCommand validCommand;

    @BeforeEach
    void setUp() {
        useCase = new RegisterSubscriberUseCase(subscriberRepository);

        keycloakUserId = UUID.randomUUID();

        validCommand = new RegisterSubscriberUseCase.RegisterCommand(
                "Jean",               // firstName
                "Rakoto",             // lastName
                "jean.rakoto@email.com", // email
                "+261341234567",      // phoneNumber
                "Lot IVK 123",        // addressLine1
                null,                 // addressLine2
                "Antananarivo",       // city
                "Ambohimanarina",     // district
                "AN",                 // regionCode
                keycloakUserId        // keycloakUserId
        );
    }

    @Nested
    @DisplayName("Successful registration")
    class SuccessfulRegistration {

        @Test
        @DisplayName("should register a new subscriber and return subscriber details")
        void shouldRegisterNewSubscriber() {
            // Given
            when(subscriberRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());
            when(subscriberRepository.findByPhoneNumber("+261341234567")).thenReturn(List.of());
            when(subscriberRepository.count()).thenReturn(42L);
            when(subscriberRepository.save(any(Subscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RegisterSubscriberUseCase.RegisterResult result = useCase.execute(validCommand);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.fullName()).isEqualTo("Jean Rakoto");
            assertThat(result.subscriberNumber()).startsWith("JRM-");

            // Verify the saved subscriber
            verify(subscriberRepository).save(subscriberCaptor.capture());
            Subscriber saved = subscriberCaptor.getValue();

            assertThat(saved.getFirstName()).isEqualTo("Jean");
            assertThat(saved.getLastName()).isEqualTo("Rakoto");
            assertThat(saved.getEmail()).isEqualTo("jean.rakoto@email.com");
            assertThat(saved.getPhoneNumber()).isEqualTo("+261341234567");
            assertThat(saved.getAddress().getLine1()).isEqualTo("Lot IVK 123");
            assertThat(saved.getAddress().getCity()).isEqualTo("Antananarivo");
            assertThat(saved.getAddress().getDistrict()).isEqualTo("Ambohimanarina");
            assertThat(saved.getAddress().getRegionCode()).isEqualTo("AN");
            assertThat(saved.getKeycloakUserId()).isEqualTo(keycloakUserId);
            assertThat(saved.getStatus()).isEqualTo(SubscriberStatus.ACTIVE);
        }

        @Test
        @DisplayName("should generate a sequential subscriber number based on count")
        void shouldGenerateSequentialSubscriberNumber() {
            // Given
            when(subscriberRepository.findByKeycloakUserId(any())).thenReturn(Optional.empty());
            when(subscriberRepository.findByPhoneNumber(anyString())).thenReturn(List.of());
            when(subscriberRepository.count()).thenReturn(99L);
            when(subscriberRepository.save(any(Subscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RegisterSubscriberUseCase.RegisterResult result = useCase.execute(validCommand);

            // Then
            // JRM-{year}-000100 (count 99 + 1 = 100, zero-padded to 6)
            int currentYear = java.time.Year.now().getValue();
            assertThat(result.subscriberNumber()).isEqualTo(String.format("JRM-%d-%06d", currentYear, 100));
        }

        @Test
        @DisplayName("should accept optional fields as null")
        void shouldAcceptOptionalFieldsAsNull() {
            // Given
            var minimalCommand = new RegisterSubscriberUseCase.RegisterCommand(
                    "Jean", "Rakoto", "jean@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null,
                    keycloakUserId
            );

            when(subscriberRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());
            when(subscriberRepository.findByPhoneNumber("+261341234567")).thenReturn(List.of());
            when(subscriberRepository.count()).thenReturn(5L);
            when(subscriberRepository.save(any(Subscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RegisterSubscriberUseCase.RegisterResult result = useCase.execute(minimalCommand);

            // Then
            assertThat(result).isNotNull();
            verify(subscriberRepository).save(subscriberCaptor.capture());
            Subscriber saved = subscriberCaptor.getValue();

            assertThat(saved.getAddress().getLine2()).isNull();
            assertThat(saved.getAddress().getDistrict()).isNull();
            assertThat(saved.getAddress().getRegionCode()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should reject duplicate Keycloak user")
        void shouldRejectDuplicateKeycloakUser() {
            // Given
            Subscriber existingSubscriber = createActiveSubscriber();
            when(subscriberRepository.findByKeycloakUserId(keycloakUserId))
                    .thenReturn(Optional.of(existingSubscriber));

            // When / Then
            assertThatThrownBy(() -> useCase.execute(validCommand))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("already linked to this Keycloak user");

            verify(subscriberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject duplicate phone number when subscriber is active")
        void shouldRejectDuplicateActivePhoneNumber() {
            // Given
            Subscriber existingWithSamePhone = createActiveSubscriber();
            when(subscriberRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());
            when(subscriberRepository.findByPhoneNumber("+261341234567"))
                    .thenReturn(List.of(existingWithSamePhone));

            // When / Then
            assertThatThrownBy(() -> useCase.execute(validCommand))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("phone number already exists");

            verify(subscriberRepository, never()).save(any());
        }

        @Test
        @DisplayName("should allow duplicate phone number when existing subscriber is inactive")
        void shouldAllowDuplicatePhoneForInactiveSubscriber() {
            // Given
            Subscriber inactiveSubscriber = createInactiveSubscriber();
            when(subscriberRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(Optional.empty());
            when(subscriberRepository.findByPhoneNumber("+261341234567"))
                    .thenReturn(List.of(inactiveSubscriber));
            when(subscriberRepository.count()).thenReturn(10L);
            when(subscriberRepository.save(any(Subscriber.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            RegisterSubscriberUseCase.RegisterResult result = useCase.execute(validCommand);

            // Then — should succeed because the existing subscriber is not active
            assertThat(result).isNotNull();
            verify(subscriberRepository).save(any(Subscriber.class));
        }
    }

    // ── Helpers ──

    private static Subscriber createActiveSubscriber() {
        return new Subscriber(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                "JRM-2026-000001", "Existing", "User",
                "existing@email.com", "+261341234567", null,
                null, null, null, SubscriberStatus.ACTIVE,
                com.jirama.domain.subscriber.enums.SubscriberType.RESIDENTIAL,
                "fr", null
        );
    }

    private static Subscriber createInactiveSubscriber() {
        return new Subscriber(
                UUID.randomUUID(), Instant.now(), Instant.now(), 0,
                "JRM-2026-000002", "Inactive", "User",
                "inactive@email.com", "+261341234567", null,
                null, null, null, SubscriberStatus.INACTIVE,
                com.jirama.domain.subscriber.enums.SubscriberType.RESIDENTIAL,
                "fr", null
        );
    }


}
