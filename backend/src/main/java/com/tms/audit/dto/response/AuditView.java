package com.tms.audit.dto.response;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.*;

public record AuditView(
    UUID id, String actor, String action, UUID resourceId, String reason, Instant occurredAt) {}
