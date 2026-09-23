package com.tms.catalog.repository;

import com.tms.catalog.entity.RouteStop;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
  List<RouteStop> findByRouteIdOrderBySequence(UUID routeId);

  void deleteByRouteId(UUID routeId);
}
