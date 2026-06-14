package com.jirama.interfaces.rest.dto.response;

import java.time.Instant;
import java.util.UUID;

public record IncidentResponse(
        UUID id,
        String incidentNumber,
        String incidentType,
        String severity,
        String status,
        String description,
        String address,
        Double locationLat,
        Double locationLng,
        Instant createdAt,
        Instant resolvedAt
) {}
