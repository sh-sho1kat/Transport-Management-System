package com.tms.service;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
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
