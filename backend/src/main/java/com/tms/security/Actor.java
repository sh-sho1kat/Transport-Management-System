package com.tms.security;

import com.tms.entity.Account;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record Actor(
    UUID id, String email, String password, String role, int version, boolean active)
    implements UserDetails {
  public static Actor of(Account a) {
    return new Actor(
        a.getId(),
        a.getEmail(),
        a.getPasswordHash(),
        a.getRole().name(),
        a.getAuthVersion(),
        a.getActive());
  }

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }

  public String getPassword() {
    return password;
  }

  public String getUsername() {
    return email;
  }

  public boolean isEnabled() {
    return active;
  }
}
