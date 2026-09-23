package com.tms.catalog.repository;

import com.tms.catalog.entity.Stop;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface StopRepository extends JpaRepository<Stop, UUID> {}
