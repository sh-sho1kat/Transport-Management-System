package com.tms.catalog.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record BusView(
    UUID id, String registration, String busType, boolean active, List<Layout> seats) {}
