package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
  Page<Booking> findByPassengerId(UUID passengerId, Pageable pageable);

  List<Booking> findByTripIdOrderByCreatedAt(UUID tripId);

  Optional<Booking> findByHoldId(UUID holdId);
}
