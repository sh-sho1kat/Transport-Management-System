package com.tms.catalog.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record StopView(UUID id, String name, String city, String address, boolean active) {}
