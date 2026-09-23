package com.tms.identity.entity;

import com.tms.identity.domain.Role;
import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "accounts")
public class Account extends BaseEntity {
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    email = value;
  }

  private String displayName;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String value) {
    displayName = value;
  }

  private String phone;

  public String getPhone() {
    return phone;
  }

  public void setPhone(String value) {
    phone = value;
  }

  private String passwordHash;

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String value) {
    passwordHash = value;
  }

  @Enumerated(EnumType.STRING)
  private Role role;

  public Role getRole() {
    return role;
  }

  public void setRole(Role value) {
    role = value;
  }

  private boolean active = true;

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    active = value;
  }

  private int authVersion;

  public int getAuthVersion() {
    return authVersion;
  }

  public void setAuthVersion(int value) {
    authVersion = value;
  }
}
