package com.tms.audit.entity;

import com.tms.identity.entity.Account;
import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "audit_events")
public class AuditEvent extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  private Account actor;

  public Account getActor() {
    return actor;
  }

  public void setActor(Account value) {
    actor = value;
  }

  private String action;

  public String getAction() {
    return action;
  }

  public void setAction(String value) {
    action = value;
  }

  private UUID resourceId;

  public UUID getResourceId() {
    return resourceId;
  }

  public void setResourceId(UUID value) {
    resourceId = value;
  }

  private String reason;

  public String getReason() {
    return reason;
  }

  public void setReason(String value) {
    reason = value;
  }
}
