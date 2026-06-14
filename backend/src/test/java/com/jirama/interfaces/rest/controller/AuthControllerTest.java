package com.jirama.interfaces.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jirama.application.auth.RegisterSubscriberUseCase;
import com.jirama.infrastructure.security.CustomAccessDeniedHandler;
import com.jirama.infrastructure.security.CustomAuthenticationEntryPoint;
import com.jirama.infrastructure.security.CustomPermissionEvaluator;
import com.jirama.infrastructure.security.KeycloakRoleConverter;
import com.jirama.infrastructure.security.SecurityConfig;
import com.jirama.interfaces.rest.dto.request.RegisterRequest;
import com.jirama.interfaces.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class, CustomPermissionEvaluator.class})
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterSubscriberUseCase registerSubscriberUseCase;

    @MockBean
    private KeycloakRoleConverter keycloakRoleConverter;

    // ── POST /api/v1/auth/register ──

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        private RegisterRequest validRequest = new RegisterRequest(
                "Jean", "Rakoto", "jean.rakoto@email.com", "+261341234567",
                "Lot IVK 123", null, "Antananarivo", "Ambohimanarina", "AN"
        );

        @Test
        @DisplayName("should return 201 with subscriber details on successful registration")
        void shouldRegisterSuccessfully() throws Exception {
            // Given
            var result = new RegisterSubscriberUseCase.RegisterResult(
                    UUID.randomUUID().toString(), "JRM-2026-000001", "Jean Rakoto"
            );
            when(registerSubscriberUseCase.execute(any())).thenReturn(result);

            // When / Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isString())
                    .andExpect(jsonPath("$.subscriberNumber").value("JRM-2026-000001"))
                    .andExpect(jsonPath("$.fullName").value("Jean Rakoto"));

            verify(registerSubscriberUseCase).execute(any());
        }

        @Test
        @DisplayName("should return 400 when firstName is blank")
        void shouldRejectBlankFirstName() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "", "Rakoto", "jean@email.com", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(registerSubscriberUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 400 when phone number format is invalid")
        void shouldRejectInvalidPhone() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "Jean", "Rakoto", "jean@email.com", "0123456789",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(registerSubscriberUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 400 when email is invalid")
        void shouldRejectInvalidEmail() throws Exception {
            var invalidRequest = new RegisterRequest(
                    "Jean", "Rakoto", "not-an-email", "+261341234567",
                    "Lot IVK 123", null, "Antananarivo", null, null
            );

            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(registerSubscriberUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldRequireAuthentication() throws Exception {
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isUnauthorized());

            verify(registerSubscriberUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 409 when use case throws duplicate exception")
        void shouldReturnConflictOnDuplicate() throws Exception {
            // Given
            when(registerSubscriberUseCase.execute(any()))
                    .thenThrow(new BusinessRuleException("DUPLICATE_KEYCLOAK_USER",
                            "A subscriber is already linked to this Keycloak user"));

            // When / Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()))
                                    .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("DUPLICATE_KEYCLOAK_USER"))
                    .andExpect(jsonPath("$.message").value("A subscriber is already linked to this Keycloak user"));
        }
    }

    // ── GET /api/v1/auth/keycloak-config ──

    @Nested
    @DisplayName("GET /api/v1/auth/keycloak-config")
    class GetKeycloakConfig {

        @Test
        @DisplayName("should return Keycloak configuration without authentication")
        void shouldReturnConfig() throws Exception {
            mockMvc.perform(get("/api/v1/auth/keycloak-config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("http://localhost:8081"))
                    .andExpect(jsonPath("$.realm").value("jirama"))
                    .andExpect(jsonPath("$.clientId").value("jirama-frontend"));
        }
    }

    // ── GET /api/v1/auth/me ──

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class GetMe {

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
                                    .claim("realm_access", java.util.Map.of("roles", java.util.List.of("CUSTOMER")))
                            ).authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
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
