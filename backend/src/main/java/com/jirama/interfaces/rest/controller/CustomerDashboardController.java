package com.jirama.interfaces.rest.controller;

import com.jirama.application.billing.CustomerDashboardUseCase;
import com.jirama.application.incident.GetMyIncidentsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Customer dashboard aggregation endpoint")
@SecurityRequirement(name = "bearerAuth")
public class CustomerDashboardController {

    private final CustomerDashboardUseCase customerDashboardUseCase;
    private final GetMyIncidentsUseCase getMyIncidentsUseCase;

    public CustomerDashboardController(CustomerDashboardUseCase customerDashboardUseCase,
                                        GetMyIncidentsUseCase getMyIncidentsUseCase) {
        this.customerDashboardUseCase = customerDashboardUseCase;
        this.getMyIncidentsUseCase = getMyIncidentsUseCase;
    }

    @GetMapping(value = "/customer", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get aggregated dashboard data for the authenticated customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDashboardUseCase.DashboardData> getCustomerDashboard(
            @AuthenticationPrincipal Jwt jwt) {
        UUID subscriberId = UUID.fromString(jwt.getSubject());

        // Count open incidents (not resolved/closed/cancelled)
        var allIncidents = getMyIncidentsUseCase.getBySubscriberId(subscriberId);
        long openCount = allIncidents.stream()
                .filter(i -> !List.of("RESOLVED", "CLOSED", "CANCELLED").contains(i.status()))
                .count();

        var dashboard = customerDashboardUseCase.getDashboard(subscriberId, (int) openCount);
        return ResponseEntity.ok(dashboard);
    }
}
