package com.tms.catalog.repository;

import com.tms.catalog.entity.Bus;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BusRepository extends JpaRepository<Bus, UUID> {
  boolean existsByRegistration(String registration);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select x from Bus x where x.id=:id")
  Optional<Bus> lockById(@Param("id") UUID id);
}
