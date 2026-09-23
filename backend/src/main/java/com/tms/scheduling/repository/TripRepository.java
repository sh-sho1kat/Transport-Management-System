package com.tms.scheduling.repository;

import com.tms.scheduling.domain.TripStatus;
import com.tms.scheduling.entity.Trip;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
  @Override
  @EntityGraph(attributePaths = {"route", "bus", "driver"})
  Page<Trip> findAll(Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Trip t where t.id=:id")
  Optional<Trip> lockById(@Param("id") UUID id);

  List<Trip> findByDriverIdOrderByDepartureAt(UUID driverId);

  boolean existsByRouteIdAndStatusNot(UUID routeId, TripStatus status);

  boolean existsByBusIdAndStatusIn(UUID busId, Collection<TripStatus> statuses);
}
