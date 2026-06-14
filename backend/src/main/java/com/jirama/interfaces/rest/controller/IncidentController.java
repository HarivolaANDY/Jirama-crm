package com.jirama.interfaces.rest.controller;

import com.jirama.application.incident.GetMyIncidentsUseCase;
import com.jirama.application.incident.ReportIncidentUseCase;
import com.jirama.domain.incident.enums.IncidentSeverity;
import com.jirama.domain.incident.enums.IncidentType;
import com.jirama.interfaces.rest.dto.request.ReportIncidentRequest;
import com.jirama.interfaces.rest.dto.response.IncidentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Service incident management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class IncidentController {

    private final ReportIncidentUseCase reportIncidentUseCase;
    private final GetMyIncidentsUseCase getMyIncidentsUseCase;

    public IncidentController(ReportIncidentUseCase reportIncidentUseCase,
                               GetMyIncidentsUseCase getMyIncidentsUseCase) {
        this.reportIncidentUseCase = reportIncidentUseCase;
        this.getMyIncidentsUseCase = getMyIncidentsUseCase;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Report a new service incident")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT')")
    public ResponseEntity<?> reportIncident(
            @Valid @RequestBody ReportIncidentRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        var command = new ReportIncidentUseCase.ReportIncidentCommand(
                request.subscriberId(),
                IncidentType.valueOf(request.incidentType()),
                IncidentSeverity.valueOf(request.severity()),
                request.description(),
                request.locationLat(),
                request.locationLng(),
                request.address(),
                request.regionId(),
                UUID.fromString(jwt.getSubject())
        );

        var result = reportIncidentUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get incidents reported by the authenticated customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<GetMyIncidentsUseCase.IncidentSummary>> getMyIncidents(
            @AuthenticationPrincipal Jwt jwt) {
        UUID subscriberId = UUID.fromString(jwt.getSubject());
        var incidents = getMyIncidentsUseCase.getBySubscriberId(subscriberId);
        return ResponseEntity.ok(incidents);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get incident by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        try {
            var incident = getMyIncidentsUseCase.getById(id);
            return ResponseEntity.ok(incident);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
