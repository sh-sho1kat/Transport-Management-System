package com.tms.shared.api.request;

import jakarta.validation.constraints.*;
import java.util.*;

public record Reason(@NotBlank @Size(max = 255) String reason) {}
