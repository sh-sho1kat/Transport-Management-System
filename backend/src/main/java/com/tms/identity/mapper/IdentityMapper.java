package com.tms.identity.mapper;

import com.tms.identity.dto.response.User;
import com.tms.identity.entity.Account;
import java.util.*;

public final class IdentityMapper {
  private IdentityMapper() {}

  public static User user(Account a) {
    return new User(
        a.getId(), a.getEmail(), a.getDisplayName(), a.getPhone(), a.getRole(), a.getActive());
  }
}
