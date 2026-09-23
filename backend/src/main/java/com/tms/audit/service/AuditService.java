package com.tms.audit.service;

import com.tms.audit.entity.AuditEvent;
import com.tms.audit.repository.AuditEventRepository;
import com.tms.identity.entity.Account;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditEventRepository events;

  public AuditService(AuditEventRepository events) {
    this.events = events;
  }

  public void record(Account actor, String action, UUID id, String reason) {
    var e = new AuditEvent();
    e.setActor(actor);
    e.setAction(action);
    e.setResourceId(id);
    e.setReason(reason);
    events.save(e);
  }
}
