package com.jirama.interfaces.rest.controller;

import com.jirama.application.auth.RegisterSubscriberUseCase;
import com.jirama.interfaces.rest.dto.request.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final RegisterSubscriberUseCase registerSubscriberUseCase;

    public AuthController(RegisterSubscriberUseCase registerSubscriberUseCase) {
        this.registerSubscriberUseCase = registerSubscriberUseCase;
    }

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Complete registration — links the authenticated Keycloak user to a subscriber record")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID keycloakUserId = UUID.fromString(jwt.getSubject());

        var command = new RegisterSubscriberUseCase.RegisterCommand(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.district(),
                request.regionCode(),
                keycloakUserId
        );

        var result = registerSubscriberUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(value = "/keycloak-config", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Keycloak configuration for the frontend SPA")
    public ResponseEntity<Map<String, String>> getKeycloakConfig() {
        return ResponseEntity.ok(Map.of(
                "url", "http://localhost:8081",
                "realm", "jirama",
                "clientId", "jirama-frontend"
        ));
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get the current authenticated user's information")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.containsKey("realm_access")
                ? ((Map<String, List<String>>) claims.get("realm_access")).getOrDefault("roles", List.of())
                : List.of();
        return ResponseEntity.ok(Map.of(
                "sub", jwt.getSubject(),
                "email", claims.getOrDefault("email", ""),
                "firstName", claims.getOrDefault("given_name", ""),
                "lastName", claims.getOrDefault("family_name", ""),
                "roles", roles
        ));
    }}
