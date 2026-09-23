package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface RouteRepository extends JpaRepository<Route, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select x from Route x where x.id=:id")
  Optional<Route> lockById(@Param("id") UUID id);
}
