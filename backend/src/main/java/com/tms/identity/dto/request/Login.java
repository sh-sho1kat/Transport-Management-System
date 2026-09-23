package com.tms.identity.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}
