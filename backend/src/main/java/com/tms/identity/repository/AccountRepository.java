package com.tms.identity.repository;

import com.tms.identity.domain.Role;
import com.tms.identity.entity.Account;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, UUID> {
  Optional<Account> findByEmail(String email);

  boolean existsByEmail(String email);

  List<Account> findByRole(Role role);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Account a where a.id=:id")
  Optional<Account> lockById(@Param("id") UUID id);
}
