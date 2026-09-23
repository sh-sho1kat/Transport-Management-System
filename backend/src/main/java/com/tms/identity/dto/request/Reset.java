package com.tms.identity.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record Reset(@NotBlank String token, @NotBlank @Size(min = 10, max = 72) String password) {}
