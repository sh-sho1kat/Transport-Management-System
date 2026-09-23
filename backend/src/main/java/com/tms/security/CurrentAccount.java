package com.tms.security;

import com.tms.entity.*;
import com.tms.entity.Types.Role;
import com.tms.exception.ApiException;
import com.tms.repository.AccountRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAccount {
  private final AccountRepository accounts;

  public CurrentAccount(AccountRepository accounts) {
    this.accounts = accounts;
  }

  public Account get() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Actor actor))
      throw new ApiException(401, "UNAUTHENTICATED", "Please sign in");
    return accounts
        .findById(actor.id())
        .filter(a -> a.getActive() && a.getAuthVersion() == actor.version())
        .orElseThrow(() -> new ApiException(401, "SESSION_EXPIRED", "Please sign in again"));
  }

  public Account require(Role role) {
    var a = get();
    if (a.getRole() != role)
      throw new ApiException(403, "FORBIDDEN", "You do not have permission for this action");
    return a;
  }
}
