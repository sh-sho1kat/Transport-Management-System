package com.tms.catalog.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record RouteView(UUID id, String code, String name, boolean active, List<StopView> stops) {}
