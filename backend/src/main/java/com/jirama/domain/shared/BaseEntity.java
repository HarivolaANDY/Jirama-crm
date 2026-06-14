package com.jirama.domain.shared;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base entity for all domain entities.
 * Provides identity (UUID), timestamps, and optimistic locking version.
 * Follows Clean Architecture — no framework annotations in the domain layer.
 */
public abstract class BaseEntity {

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    protected BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0;
    }

    protected BaseEntity(UUID id, Instant createdAt, Instant updatedAt, long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    protected void markUpdated() {
        this.updatedAt = Instant.now();
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}
