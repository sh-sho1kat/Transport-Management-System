package com.tms.identity.dto.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record Recovery(@NotBlank @Email String email) {}
