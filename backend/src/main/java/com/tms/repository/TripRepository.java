package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Trip t where t.id=:id")
  Optional<Trip> lockById(@Param("id") UUID id);

  List<Trip> findByDriverIdOrderByDepartureAt(UUID driverId);

  boolean existsByRouteIdAndStatusNot(UUID routeId, TripStatus status);

  boolean existsByBusIdAndStatusIn(UUID busId, Collection<TripStatus> statuses);
}
