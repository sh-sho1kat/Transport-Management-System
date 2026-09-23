package com.tms.shared.api.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record FieldError(String field, String message) {}
