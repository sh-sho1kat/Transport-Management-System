package com.tms.service.impl;

import com.tms.dto.request.*;
import com.tms.dto.response.*;
import com.tms.entity.Seat;
import com.tms.exception.ApiException;
import com.tms.mapper.*;
import com.tms.repository.SeatRepository;
import com.tms.service.SeatService;
import java.time.*;
import java.time.format.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SeatServiceImpl implements SeatService {
    private final SeatRepository repository;
    private final SeatMapper mapper;
    private final SeatRequestMapper requestMapper;
    private final Clock clock;
    private final DateTimeFormatter timeFormat;
    public SeatServiceImpl(SeatRepository repository, SeatMapper mapper, SeatRequestMapper requestMapper, Clock clock,
                           @Value("${tms.booking-zone}") String zone, @Value("${tms.booking-locale}") String locale) {
        this.repository = repository; this.mapper = mapper; this.requestMapper = requestMapper; this.clock = clock;
        this.timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
                .withLocale(Locale.forLanguageTag(locale)).withZone(ZoneId.of(zone));
    }
    public SeatInitializationResponse initialize(String tripId) {
        return ServiceOperation.seat("Failed to create seats.", () -> {
            List<Seat> seats = new ArrayList<>();
            for (int i = 1; i <= 40; i++) seats.add(new Seat(null, String.format(Locale.ROOT, "%02d", i),
                    "unbooked", null, null, null, null, 0));
            repository.replaceAll(tripId, seats);
            return new SeatInitializationResponse("Seats created successfully for trip " + tripId + "!", tripId);
        });
    }
    public List<SeatResponse> findAll(String tripId) {
        return ServiceOperation.seat("Failed to fetch seats.", () -> responses(repository.findAll(tripId)));
    }
    public List<SeatResponse> findBooked(String tripId) {
        return ServiceOperation.seat("Failed to fetch booked seats.", () -> responses(repository.findBooked(tripId)));
    }
    public List<SeatResponse> findByStudentId(String tripId, String studentId) {
        return ServiceOperation.seat("Failed to fetch student bookings.", () -> {
            var seats = repository.findByStudentId(tripId, studentId);
            if (seats.isEmpty()) throw ApiException.message(404, "No bookings found for this student in this trip");
            return responses(seats);
        });
    }
    public SeatResponse findBySeatNo(String tripId, String seatNo) {
        return ServiceOperation.seat("Failed to fetch seat.", () -> mapper.toResponse(
                repository.findBySeatNo(tripId, seatNo).orElseThrow(this::notFound)));
    }
    public SeatUpdateResponse update(String tripId, String seatNo, SeatUpdateRequest request) {
        return ServiceOperation.seat("Failed to update seat.", () -> {
            if (request == null) throw ApiException.message(400, "Booking status is required");
            RequestValues.require("Booking status is required", request.bookingStatus());
            if (requestMapper.needsStudent(request)) throw ApiException.message(400, "Student details are required for booking");
            Instant now = clock.instant();
            Seat result = repository.update(tripId, seatNo, requestMapper.toChanges(request, now, formatTime(now)))
                    .orElseThrow(this::notFound);
            return new SeatUpdateResponse("Seat updated successfully", mapper.toResponse(result));
        });
    }
    public BulkSeatUpdateResponse updateMany(String tripId, BulkSeatUpdateRequest request) {
        if (request == null || request.seats() == null || !request.seats().isArray() || request.seats().isEmpty())
            throw ApiException.message(400, "Invalid seats data");
        Instant now = clock.instant();
        String time = formatTime(now);
        List<SeatResponse> results = new ArrayList<>();
        RuntimeException firstFailure = null;
        // Preserve nontransactional partial writes and null entries for missing seats.
        for (var node : request.seats()) {
            try {
                var seat = requestMapper.fromBulkElement(node);
                if (requestMapper.needsStudent(seat)) throw new IllegalArgumentException("Student details required for seat "
                        + (seat.seatNo() == null ? "undefined" : seat.seatNo().asText()));
                var updated = repository.update(tripId, RequestValues.string(seat.seatNo()), requestMapper.toChanges(seat, now, time));
                results.add(updated.map(mapper::toResponse).orElse(null));
            } catch (RuntimeException e) { if (firstFailure == null) firstFailure = e; }
        }
        if (firstFailure != null) throw new ApiException(500, "error",
                firstFailure.getMessage() == null ? "Failed to update seats." : firstFailure.getMessage());
        return new BulkSeatUpdateResponse("Seats updated successfully", results, tripId);
    }
    private String formatTime(Instant now) { return timeFormat.format(now).replace('\u202f', ' '); }
    private List<SeatResponse> responses(List<Seat> seats) { return seats.stream().map(mapper::toResponse).toList(); }
    private ApiException notFound() { return ApiException.message(404, "Seat not found"); }
}
