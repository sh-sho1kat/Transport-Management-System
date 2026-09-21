package com.tms.entity;

import java.time.Instant;

/** Explicit patch semantics distinguish an omitted status from a supplied null value. */
public record SeatChanges(boolean statusProvided, String bookingStatus, boolean replaceBookingDetails,
                          String studentId, String studentMail, Instant bookingDate, String bookingTime) {}
