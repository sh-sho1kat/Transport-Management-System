package com.tms.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
public abstract class BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  public UUID getId() {
    return id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
