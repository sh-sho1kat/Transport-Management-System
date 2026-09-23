package com.tms.repository;

import com.tms.entity.*;
import com.tms.entity.Types.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<ResetToken> findByTokenHash(String tokenHash);
}
