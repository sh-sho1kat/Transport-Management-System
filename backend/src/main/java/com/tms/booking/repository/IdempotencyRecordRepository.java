package com.tms.booking.repository;

import com.tms.booking.entity.IdempotencyRecord;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {
  Optional<IdempotencyRecord> findByActorIdAndRequestKey(UUID actorId, String requestKey);
}
