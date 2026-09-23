package com.tms.scheduling.dto.response;

import com.tms.scheduling.domain.SeatStatus;
import jakarta.validation.constraints.*;
import java.util.*;

public record SeatView(String label, int rowNumber, int columnNumber, SeatStatus status) {}
