package com.jirama.interfaces.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirama.domain.subscriber.Subscriber;
import com.jirama.domain.subscriber.SubscriberRepository;
import com.jirama.domain.subscriber.enums.SubscriberStatus;
import com.jirama.interfaces.rest.dto.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration test for AuthController.
 * Uses TestContainers (via application-test.yml) for a real PostgreSQL database.
 * Runs Flyway migrations and exercises the complete HTTP + service + repository pipeline.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UUID keycloakUserId;

    /**
     * Stub the JwtDecoder so the application context loads.
     * The actual JWT mock comes from .with(jwt()), which bypasses the decoder.
     */
    @BeforeEach
    void setUp() {
        keycloakUserId = UUID.randomUUID();

        when(jwtDecoder.decode(anyString())).thenReturn(
                Jwt.withTokenValue("mock-token")
                        .header("alg", "RS256")
                        .claim("sub", keycloakUserId.toString())
                        .claim("email", "test@jirama.mg")
                        .claim("given_name", "Test")
                        .claim("family_name", "User")
                        .claim("realm_access", Map.of("roles", List.of("CUSTOMER")))
                        .build()
        );
    }

    // ── GET /api/v1/auth/keycloak-config (public) ──

    @Nested
    @DisplayName("GET /api/v1/auth/keycloak-config")
    class KeycloakConfig {

        @Test
        @DisplayName("should return Keycloak configuration without authentication")
        void shouldReturnConfig() throws Exception {
            mockMvc.perform(get("/api/v1/auth/keycloak-config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("http://localhost:8081"))
                    .andExpect(jsonPath("$.realm").value("jirama"))
                    .andExpect(jsonPath("$.clientId").value("jirama-frontend"));
        }

        @Test
        @DisplayName("should return JSON content type")
        void shouldReturnJson() throws Exception {
            mockMvc.perform(get("/api/v1/auth/keycloak-config"))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }

    // ── POST /api/v1/auth/register ──

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        private RegisterRequest validRequest;

        @BeforeEach
        void setUp() {
            validRequest = new RegisterRequest(
                    "Jean", "Rakoto", "jean.rakoto@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", "Ambohimanarina", "AN"
            );
        }

        @Test
        @DisplayName("should register a new subscriber and persist to database")
        void shouldRegisterAndPersist() throws Exception {
            // When — register via HTTP
            var result = mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder
                                            .subject(keycloakUserId.toString())
                                            .claim("email", "jean.rakoto@email.com")
                                            .claim("given_name", "Jean")
                                            .claim("family_name", "Rakoto")
                                    )
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isString())
                    .andExpect(jsonPath("$.subscriberNumber").isString())
                    .andExpect(jsonPath("$.fullName").value("Jean Rakoto"))
                    .andReturn();

            // Then — extract the ID and verify DB persistence
            String responseBody = result.getResponse().getContentAsString();
            String subscriberId = objectMapper.readTree(responseBody).get("id").asText();

            Optional<Subscriber> fetched = subscriberRepository.findById(UUID.fromString(subscriberId));
            assertThat(fetched).isPresent();
            assertThat(fetched.get().getFirstName()).isEqualTo("Jean");
            assertThat(fetched.get().getLastName()).isEqualTo("Rakoto");
            assertThat(fetched.get().getEmail()).isEqualTo("jean.rakoto@email.com");
            assertThat(fetched.get().getPhoneNumber()).isEqualTo("+261341234567");
            assertThat(fetched.get().getKeycloakUserId()).isEqualTo(keycloakUserId);
            assertThat(fetched.get().getStatus()).isEqualTo(SubscriberStatus.ACTIVE);
            assertThat(fetched.get().getSubscriberNumber()).startsWith("JRM-");
        }

        @Test
        @DisplayName("should return 400 for blank required fields")
        void shouldRejectBlankFirstName() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "", "Rakoto", "jean@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for invalid phone number format")
        void shouldRejectInvalidPhone() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "Jean", "Rakoto", "jean@email.com", "0123456789",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for invalid email")
        void shouldRejectInvalidEmail() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "Jean", "Rakoto", "not-an-email", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 409 when Keycloak user is already linked to a subscriber")
        void shouldReturnConflictForDuplicateKeycloakUser() throws Exception {
            // Given — register the same Keycloak user twice
            var firstRequest = new RegisterRequest(
                    "Jean", "Rakoto", "jean@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            var secondRequest = new RegisterRequest(
                    "Pierre", "Randria", "pierre@email.com", "+261349876543",
                    "Lot ABC 456", null, "Antananarivo", null, null
            );

            var jwtPostProcessor = jwt().jwt(builder -> builder.subject(keycloakUserId.toString()));

            // First registration succeeds
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwtPostProcessor.authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());

            // Second registration with same Keycloak user fails
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwtPostProcessor.authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_KEYCLOAK_USER"))
                    .andExpect(jsonPath("$.message").value("A subscriber is already linked to this Keycloak user"));
        }

        @Test
        @DisplayName("should return 409 when phone number is already in use by an active subscriber")
        void shouldReturnConflictForDuplicateActivePhone() throws Exception {
            // Given — register with a phone number
            var firstRequest = new RegisterRequest(
                    "Jean", "Rakoto", "jean@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            // Register again with same phone but different Keycloak user
            var duplicateRequest = new RegisterRequest(
                    "Pierre", "Randria", "pierre@email.com", "+261341234567",
                    "Lot ABC 456", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(firstRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("A subscriber with this phone number already exists"));
        }

        @Test
        @DisplayName("should generate unique subscriber numbers for sequential registrations")
        void shouldGenerateUniqueNumbers() throws Exception {
            // When — register two users
            var user1 = new RegisterRequest(
                    "Jean", "Rakoto", "jean1@email.com", "+261341234561",
                    "Addr 1", null, "City", null, null
            );
            var user2 = new RegisterRequest(
                    "Pierre", "Randria", "pierre2@email.com", "+261341234562",
                    "Addr 2", null, "City", null, null
            );

            var result1 = mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user1)))
                    .andExpect(status().isCreated())
                    .andReturn();

            var result2 = mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(user2)))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Then — subscriber numbers should be different
            String num1 = objectMapper.readTree(result1.getResponse().getContentAsString())
                    .get("subscriberNumber").asText();
            String num2 = objectMapper.readTree(result2.getResponse().getContentAsString())
                    .get("subscriberNumber").asText();

            assertThat(num1).isNotEqualTo(num2);
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldRequireAuthentication() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /api/v1/auth/me ──

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class Me {

        @Test
        @DisplayName("should return user info from JWT claims")
        void shouldReturnUserInfo() throws Exception {
            String subject = UUID.randomUUID().toString();

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(jwt().jwt(builder -> builder
                                            .subject(subject)
                                            .claim("email", "jean@email.com")
                                            .claim("given_name", "Jean")
                                            .claim("family_name", "Rakoto")
                                            .claim("realm_access", Map.of("roles", List.of("CUSTOMER")))
                                    )
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sub").value(subject))
                    .andExpect(jsonPath("$.email").value("jean@email.com"))
                    .andExpect(jsonPath("$.firstName").value("Jean"))
                    .andExpect(jsonPath("$.lastName").value("Rakoto"))
                    .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
        }

        @Test
        @DisplayName("should return empty defaults when claims are missing")
        void shouldHandleMissingClaims() throws Exception {
            String subject = UUID.randomUUID().toString();

            mockMvc.perform(get("/api/v1/auth/me")
                            .with(jwt().jwt(builder -> builder.subject(subject))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sub").value(subject))
                    .andExpect(jsonPath("$.email").value(""))
                    .andExpect(jsonPath("$.roles").isEmpty());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldRequireAuthentication() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
