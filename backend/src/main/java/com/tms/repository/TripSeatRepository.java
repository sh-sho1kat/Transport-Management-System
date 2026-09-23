package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
  List<TripSeat> findByTripIdOrderByRowNumberAscColumnNumberAsc(UUID tripId);

  List<TripSeat> findByHoldId(UUID holdId);

  List<TripSeat> findByBookingId(UUID bookingId);
}
