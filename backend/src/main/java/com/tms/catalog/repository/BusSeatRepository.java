package com.tms.catalog.repository;

import com.tms.catalog.entity.BusSeat;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface BusSeatRepository extends JpaRepository<BusSeat, UUID> {
  List<BusSeat> findByBusIdOrderByRowNumberAscColumnNumberAsc(UUID busId);

  void deleteByBusId(UUID busId);
}
