package com.tms.scheduling.repository;

import com.tms.scheduling.entity.TripSeat;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface TripSeatRepository extends JpaRepository<TripSeat, UUID> {
  List<TripSeat> findByTripIdOrderByRowNumberAscColumnNumberAsc(UUID tripId);

  List<TripSeat> findByHoldId(UUID holdId);

  List<TripSeat> findByBookingId(UUID bookingId);

  interface InventoryCount {
    java.util.UUID getTripId();

    long getSellable();

    long getBooked();
  }

  @Query(
      "select s.trip.id as tripId, sum(case when s.status <>"
          + " com.tms.scheduling.domain.SeatStatus.BLOCKED then 1 else 0 end) as sellable, sum(case"
          + " when s.status = com.tms.scheduling.domain.SeatStatus.BOOKED then 1 else 0 end) as"
          + " booked from TripSeat s where s.trip.id in :ids group by s.trip.id")
  List<InventoryCount> aggregateInventory(
      @org.springframework.data.repository.query.Param("ids") Collection<UUID> ids);
}
