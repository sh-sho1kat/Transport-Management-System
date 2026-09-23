package com.tms.entity;

import com.tms.entity.Types.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "reset_tokens")
public class ResetToken extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account value) {
    account = value;
  }

  private String tokenHash;

  public String getTokenHash() {
    return tokenHash;
  }

  public void setTokenHash(String value) {
    tokenHash = value;
  }

  private Instant expiresAt;

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant value) {
    expiresAt = value;
  }

  private boolean consumed;

  public boolean getConsumed() {
    return consumed;
  }

  public void setConsumed(boolean value) {
    consumed = value;
  }
}
