package com.tms.identity.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record Csrf(String token, String headerName) {}
