package com.jirama.interfaces.rest.controller;

import com.jirama.application.subscriber.CreateSubscriberUseCase;
import com.jirama.application.subscriber.SearchSubscriberUseCase;
import com.jirama.domain.subscriber.enums.SubscriberType;
import com.jirama.interfaces.rest.dto.request.CreateSubscriberRequest;
import com.jirama.interfaces.rest.dto.response.SubscriberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscribers")
@Tag(name = "Subscribers", description = "Subscriber management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubscriberController {

    private final CreateSubscriberUseCase createSubscriberUseCase;
    private final SearchSubscriberUseCase searchSubscriberUseCase;

    public SubscriberController(CreateSubscriberUseCase createSubscriberUseCase,
                                 SearchSubscriberUseCase searchSubscriberUseCase) {
        this.createSubscriberUseCase = createSubscriberUseCase;
        this.searchSubscriberUseCase = searchSubscriberUseCase;
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new subscriber")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<CreateSubscriberUseCase.CreateSubscriberResult> create(
            @Valid @RequestBody CreateSubscriberRequest request) {

        var command = new CreateSubscriberUseCase.CreateSubscriberCommand(
                request.firstName(), request.lastName(), request.email(),
                request.phoneNumber(), request.idCardNumber(),
                request.addressLine1(), request.addressLine2(),
                request.city(), request.district(), request.regionCode(),
                request.postalCode(),
                SubscriberType.valueOf(request.subscriberType()),
                request.preferredLanguage() != null ? request.preferredLanguage() : "fr"
        );

        var result = createSubscriberUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Search subscribers")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<List<SearchSubscriberUseCase.SubscriberSummary>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var query = new SearchSubscriberUseCase.SearchQuery(q, page, size);
        var results = searchSubscriberUseCase.execute(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get subscriber by ID")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<SubscriberResponse> getById(@PathVariable UUID id) {
        // TODO: Implement getById use case
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get own subscriber profile (for customers)")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<SubscriberResponse> getMyProfile() {
        // TODO: Implement get my profile use case
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
