package com.tms.identity.dto.request;

import com.tms.identity.domain.Role;
import jakarta.validation.constraints.*;
import java.util.*;

public record Staff(
    @NotBlank @Email @Size(max = 254) String email,
    @NotBlank @Size(min = 10, max = 72) String password,
    @NotBlank @Size(max = 100) String displayName,
    @NotBlank @Size(max = 30) String phone,
    @NotNull Role role) {}
