package com.jirama.interfaces.rest.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SubscriberResponse(
        UUID id,
        String subscriberNumber,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String city,
        String regionCode,
        String status,
        String subscriberType,
        String preferredLanguage,
        Instant createdAt,
        Instant updatedAt
) {}
