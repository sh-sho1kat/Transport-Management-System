package com.tms.identity.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record Profile(
    @NotBlank @Size(max = 100) String displayName, @NotBlank @Size(max = 30) String phone) {}
