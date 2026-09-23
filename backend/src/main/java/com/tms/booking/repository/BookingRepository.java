package com.tms.booking.repository;

import com.tms.booking.entity.Booking;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
  Page<Booking> findByPassengerId(UUID passengerId, Pageable pageable);

  List<Booking> findByTripIdOrderByCreatedAt(UUID tripId);

  Optional<Booking> findByHoldId(UUID holdId);

  interface BookingCount {
    java.util.UUID getTripId();

    long getConfirmed();

    long getCancelled();

    long getValue();
  }

  @Query(
      "select b.trip.id as tripId, sum(case when b.status ="
          + " com.tms.booking.domain.BookingStatus.CONFIRMED then 1 else 0 end) as confirmed,"
          + " sum(case when b.status = com.tms.booking.domain.BookingStatus.CANCELLED then 1 else 0"
          + " end) as cancelled, sum(case when b.status ="
          + " com.tms.booking.domain.BookingStatus.CONFIRMED then b.amountMinor else 0 end) as"
          + " value from Booking b where b.trip.id in :ids group by b.trip.id")
  List<BookingCount> aggregateBookings(
      @org.springframework.data.repository.query.Param("ids") Collection<UUID> ids);
}
