package com.tms.identity.dto.response;

import com.tms.identity.domain.Role;
import jakarta.validation.constraints.*;
import java.util.*;

public record User(
    UUID id, String email, String displayName, String phone, Role role, boolean active) {}
