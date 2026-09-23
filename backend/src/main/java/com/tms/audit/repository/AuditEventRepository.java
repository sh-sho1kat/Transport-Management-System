package com.tms.audit.repository;

import com.tms.audit.entity.AuditEvent;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {}
