package com.tms.identity.service;

import com.tms.identity.dto.request.Profile;
import com.tms.identity.dto.request.Register;
import com.tms.identity.dto.request.Reset;
import com.tms.identity.dto.request.Staff;
import com.tms.identity.dto.response.User;
import com.tms.shared.api.request.Active;
import java.util.List;

public interface IdentityService {
  User register(Register request);

  User me();

  User profile(Profile request);

  User staff(Staff request);

  List<User> staff();

  void requestReset(String email);

  void reset(Reset request);

  User active(java.util.UUID id, Active input);
}
