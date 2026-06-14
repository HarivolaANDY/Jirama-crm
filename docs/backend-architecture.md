# Backend Architecture — Spring Boot + Clean Architecture

## Package Structure

```
com.jirama
├── JiramaApplication.java              # Main entry point
│
├── domain/                             # DOMAIN LAYER (Pure Java)
│   ├── subscriber/
│   │   ├── Subscriber.java             # Aggregate root
│   │   ├── Contract.java               # Entity
│   │   ├── Meter.java                  # Entity
│   │   ├── ContractStatus.java         # Value Object (enum)
│   │   ├── MeterType.java              # Value Object (enum)
│   │   ├── SubscriberRepository.java   # Output Port (interface)
│   │   └── SubscriberService.java      # Domain Service
│   │
│   ├── billing/
│   │   ├── Invoice.java                # Aggregate root
│   │   ├── Payment.java                # Entity
│   │   ├── PaymentMethod.java          # Value Object (enum)
│   │   ├── InvoiceStatus.java          # Value Object (enum)
│   │   ├── BillingPeriod.java          # Value Object
│   │   ├── ConsumptionReading.java     # Value Object
│   │   ├── InvoiceRepository.java      # Output Port
│   │   └── BillingService.java         # Domain Service
│   │
│   ├── incident/
│   │   ├── Incident.java               # Aggregate root
│   │   ├── Complaint.java              # Aggregate root
│   │   ├── IncidentType.java           # Value Object (enum)
│   │   ├── IncidentSeverity.java       # Value Object (enum)
│   │   ├── IncidentStatus.java         # Value Object (enum)
│   │   ├── IncidentRepository.java     # Output Port
│   │   └── ComplaintRepository.java    # Output Port
│   │
│   ├── user/
│   │   ├── User.java                   # Entity
│   │   ├── UserRole.java               # Value Object (enum: CUSTOMER, AGENT, TECHNICIAN, ADMIN)
│   │   ├── Agent.java                  # Entity (extends User)
│   │   ├── Technician.java             # Entity (extends User)
│   │   ├── UserRepository.java         # Output Port
│   │   └── UserService.java            # Domain Service
│   │
│   ├── notification/
│   │   ├── Notification.java           # Entity
│   │   ├── NotificationChannel.java    # Value Object (enum)
│   │   ├── NotificationTemplate.java   # Entity
│   │   └── NotificationRepository.java # Output Port
│   │
│   ├── document/
│   │   ├── Document.java               # Aggregate root
│   │   ├── DocumentType.java           # Value Object (enum)
│   │   └── DocumentRepository.java     # Output Port
│   │
│   └── shared/
│       ├── BaseEntity.java             # Abstract base (id, createdAt, updatedAt)
│       ├── Money.java                  # Value Object (amount + currency)
│       ├── Address.java                # Value Object
│       ├── PhoneNumber.java            # Value Object
│       ├── Email.java                  # Value Object
│       └── AuditLog.java              # Entity
│
├── application/                        # APPLICATION LAYER
│   ├── subscriber/
│   │   ├── CreateSubscriberUseCase.java
│   │   ├── UpdateSubscriberUseCase.java
│   │   ├── SearchSubscriberUseCase.java
│   │   ├── AssignMeterUseCase.java
│   │   └── SuspendContractUseCase.java
│   │
│   ├── billing/
│   │   ├── GenerateInvoiceUseCase.java
│   │   ├── ProcessPaymentUseCase.java
│   │   ├── GetInvoiceUseCase.java
│   │   ├── GetPaymentHistoryUseCase.java
│   │   └── DownloadInvoicePdfUseCase.java
│   │
│   ├── incident/
│   │   ├── ReportIncidentUseCase.java
│   │   ├── ResolveIncidentUseCase.java
│   │   ├── TrackIncidentUseCase.java
│   │   ├── FileComplaintUseCase.java
│   │   └── ProcessComplaintUseCase.java
│   │
│   ├── notification/
│   │   ├── SendNotificationUseCase.java
│   │   ├── GetNotificationsUseCase.java
│   │   └── MarkNotificationReadUseCase.java
│   │
│   ├── reporting/
│   │   ├── GenerateReportUseCase.java
│   │   ├── GetDashboardStatsUseCase.java
│   │   └── ExportReportUseCase.java
│   │
│   └── auth/
│       ├── LoginUseCase.java
│       ├── RegisterUserUseCase.java
│       └── ManageUserRoleUseCase.java
│
├── infrastructure/                     # INFRASTRUCTURE / ADAPTERS
│   ├── persistence/
│   │   ├── JpaSubscriberRepository.java         # Implements SubscriberRepository
│   │   ├── JpaInvoiceRepository.java            # Implements InvoiceRepository
│   │   ├── JpaIncidentRepository.java           # Implements IncidentRepository
│   │   ├── entity/
│   │   │   ├── SubscriberEntity.java            # JPA @Entity
│   │   │   ├── ContractEntity.java              # JPA @Entity
│   │   │   ├── MeterEntity.java                 # JPA @Entity
│   │   │   ├── InvoiceEntity.java               # JPA @Entity
│   │   │   ├── PaymentEntity.java               # JPA @Entity
│   │   │   ├── IncidentEntity.java              # JPA @Entity
│   │   │   ├── ComplaintEntity.java             # JPA @Entity
│   │   │   ├── UserEntity.java                  # JPA @Entity
│   │   │   ├── NotificationEntity.java          # JPA @Entity
│   │   │   └── AuditLogEntity.java              # JPA @Entity
│   │   └── mapper/
│   │       ├── SubscriberMapper.java            # Entity ↔ Domain
│   │       ├── InvoiceMapper.java
│   │       └── IncidentMapper.java
│   │
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── KeycloakRoleConverter.java
│   │   ├── SecurityConfig.java
│   │   └── CurrentUser.java                     # @AuthenticationPrincipal wrapper
│   │
│   ├── document/
│   │   └── MinioDocumentStorage.java            # Implements DocumentRepository
│   │
│   ├── cache/
│   │   └── RedisCacheService.java
│   │
│   └── messaging/
│       ├── NotificationSender.java
│       ├── EmailNotificationSender.java
│       └── SmsNotificationSender.java
│
├── interfaces/                         # INTERFACE ADAPTERS
│   ├── rest/
│   │   ├── controller/
│   │   │   ├── SubscriberController.java
│   │   │   ├── InvoiceController.java
│   │   │   ├── PaymentController.java
│   │   │   ├── IncidentController.java
│   │   │   ├── ComplaintController.java
│   │   │   ├── NotificationController.java
│   │   │   ├── DocumentController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── ReportController.java
│   │   │   ├── AuthController.java
│   │   │   └── AgentController.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── CreateSubscriberRequest.java
│   │   │   │   ├── ProcessPaymentRequest.java
│   │   │   │   ├── ReportIncidentRequest.java
│   │   │   │   └── SearchSubscriberRequest.java
│   │   │   └── response/
│   │   │       ├── SubscriberResponse.java
│   │   │       ├── InvoiceResponse.java
│   │   │       ├── IncidentResponse.java
│   │   │       ├── DashboardStatsResponse.java
│   │   │       └── PaginatedResponse.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── ResourceNotFoundException.java
│   │       ├── ValidationException.java
│   │       └── BusinessRuleException.java
│   │
│   └── openapi/
│       └── OpenApiConfig.java
│
├── shared/                             # SHARED KERNEL
│   ├── annotations/
│   │   └── UseCase.java                       # @Component stereotype
│   └── util/
│       ├── DateUtils.java
│       └── PdfGenerator.java
│
└── resources/
    ├── application.yml
    ├── application-dev.yml
    ├── application-prod.yml
    └── db/migration/
        ├── V1__create_users_tables.sql
        ├── V2__create_subscriber_tables.sql
        ├── V3__create_billing_tables.sql
        ├── V4__create_incident_tables.sql
        ├── V5__create_notification_tables.sql
        ├── V6__create_document_tables.sql
        ├── V7__create_audit_tables.sql
        └── V8__create_indexes.sql
```

## Layer Communication Rules

```
Controller (Interface Layer)
    │
    │ Calls Use Case (Application Layer)
    ▼
Use Case (Application Layer)
    │
    │ Orchestrates Domain + Repositories
    ▼
Domain Service / Entity (Domain Layer)
    │
    │ Uses Ports (Interfaces)
    ▼
Repository Implementation (Infrastructure Layer)
    │
    │ JPA / PostgreSQL
    ▼
Database
```

**Key Constraints:**
- Controllers NEVER access repositories directly
- Use cases receive domain objects, return DTOs
- Domain entities are NEVER exposed to controllers
- Mappers convert between domain ↔ JPA entities ↔ DTOs
- Transaction boundaries are at the use case level

## Validation Strategy

| Layer | Validation | Technology |
|---|---|---|
| Controller | Request DTO validation | Jakarta Validation (`@Valid`, `@NotBlank`, etc.) |
| Application | Business rule validation | Custom validators in use cases |
| Domain | Invariant validation | Domain object constructors & methods |
| Database | Constraint validation | DB constraints (UNIQUE, NOT NULL, FK) |

## Error Handling

```
GlobalExceptionHandler (@ControllerAdvice)
├── ResourceNotFoundException    → 404
├── ValidationException          → 400 + field errors
├── BusinessRuleException        → 409 Conflict
├── AccessDeniedException        → 403
└── Generic Exception            → 500 (logged, sanitized response)
```

All errors return a consistent JSON structure:
```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "timestamp": "2026-06-14T10:30:00Z",
  "path": "/api/v1/subscribers",
  "errors": [
    { "field": "email", "message": "must be a valid email address" }
  ]
}
```

## Audit & Logging

- **AOP-based `@AuditLog` annotation** on use cases
- Logs: user ID, action, entity type, entity ID, old/new values, timestamp
- Stored in `audit_logs` table (immutable, append-only)
- Application logs structured as JSON (via Logback + Logstash encoder)
- Log levels: ERROR (production issues), WARN (validation failures), INFO (use case execution)
