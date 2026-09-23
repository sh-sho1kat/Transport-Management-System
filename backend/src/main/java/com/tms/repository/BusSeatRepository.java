package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface BusSeatRepository extends JpaRepository<BusSeat, UUID> {
  List<BusSeat> findByBusIdOrderByRowNumberAscColumnNumberAsc(UUID busId);

  void deleteByBusId(UUID busId);
}
