package com.jirama.interfaces.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReportIncidentRequest(
        UUID subscriberId,

        @NotBlank(message = "Incident type is required")
        String incidentType,

        @NotBlank(message = "Severity is required")
        String severity,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
        String description,

        Double locationLat,
        Double locationLng,

        @Size(max = 255)
        String address,

        UUID regionId
) {}
