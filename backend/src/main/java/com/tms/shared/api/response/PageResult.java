package com.tms.shared.api.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record PageResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {}
