package com.jirama.interfaces.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSubscriberRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+261\\d{9}$", message = "Phone number must be a valid Malagasy number (+261 XX XXX XX XX)")
        String phoneNumber,

        @Size(max = 50, message = "ID card number must not exceed 50 characters")
        String idCardNumber,

        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        String district,

        @Size(max = 20, message = "Region code must not exceed 20 characters")
        String regionCode,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode,

        @NotBlank(message = "Subscriber type is required")
        @Pattern(regexp = "RESIDENTIAL|COMMERCIAL|INDUSTRIAL|GOVERNMENT",
                message = "Subscriber type must be one of: RESIDENTIAL, COMMERCIAL, INDUSTRIAL, GOVERNMENT")
        String subscriberType,

        @Pattern(regexp = "fr|mg", message = "Preferred language must be 'fr' or 'mg'")
        String preferredLanguage
) {}
