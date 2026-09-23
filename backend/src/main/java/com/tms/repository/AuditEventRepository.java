package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {}
