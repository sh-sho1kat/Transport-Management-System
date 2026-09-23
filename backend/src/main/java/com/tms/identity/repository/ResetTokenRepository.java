package com.tms.identity.repository;

import com.tms.identity.entity.ResetToken;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<ResetToken> findByTokenHash(String tokenHash);
}
