package com.tms.booking.repository;

import com.tms.booking.domain.HoldStatus;
import com.tms.booking.entity.Hold;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface HoldRepository extends JpaRepository<Hold, UUID> {
  List<Hold> findByTripIdAndStatus(UUID tripId, HoldStatus status);

  @Query("select distinct h.trip.id from Hold h where h.status=:status and h.expiresAt<:now")
  List<UUID> expiredTripIds(@Param("status") HoldStatus status, @Param("now") Instant now);
}
