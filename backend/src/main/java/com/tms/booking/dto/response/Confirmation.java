package com.tms.booking.dto.response;

import jakarta.validation.constraints.*;
import java.util.*;

public record Confirmation(BookingView booking, boolean replayed) {}
