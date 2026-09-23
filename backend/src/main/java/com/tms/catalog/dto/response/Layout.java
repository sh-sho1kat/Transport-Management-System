package com.tms.catalog.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record Layout(String label, int rowNumber, int columnNumber, boolean blocked) {}
