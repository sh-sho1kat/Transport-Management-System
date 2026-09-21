package com.tms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.dto.request.*;
import com.tms.entity.Seat;
import com.tms.entity.SeatChanges;
import com.tms.exception.ApiException;
import com.tms.mapper.*;
import com.tms.repository.*;
import com.tms.service.impl.*;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Business behavior can be tested without starting Spring, HTTP, or MongoDB. */
class ServiceIsolationTest {
    private final ObjectMapper json = new ObjectMapper();
    private final Instant now = Instant.parse("2026-09-21T08:15:30Z");
    private SeatService service(SeatRepository repository) {
        return new SeatServiceImpl(repository, new SeatMapper(), new SeatRequestMapper(),
                Clock.fixed(now, ZoneOffset.UTC), "Asia/Dhaka", "en-US");
    }
    @Test void invalidTripNeverTouchesPersistenceEvenWithInvalidId() {
        TripRepository repository = mock(TripRepository.class);
        TripService service = new TripServiceImpl(repository, new TripMapper());
        assertThatThrownBy(() -> service.update("not-an-id", new TripRequest(null,null,null,null,null,null)))
                .isInstanceOf(ApiException.class).hasMessage("All fields are required");
        verifyNoInteractions(repository);
    }
    @Test void bookingUsesInjectedServerTimeAndMappedResponse() throws Exception {
        SeatRepository repository = mock(SeatRepository.class);
        when(repository.update(eq("TRIP1"), eq("01"), any())).thenAnswer(invocation -> {
            SeatChanges c = invocation.getArgument(2);
            return Optional.of(new Seat("012345678901234567890123", "01", c.bookingStatus(), c.studentId(),
                    c.studentMail(), c.bookingDate(), c.bookingTime(), 0));
        });
        SeatUpdateRequest request = json.readValue("""
                {"bookingStatus":"booked","studentId":"12345","studentMail":"student@example.test",
                 "bookingDate":"2000-01-01T00:00:00Z"}
                """, SeatUpdateRequest.class);
        var result = service(repository).update("TRIP1", "01", request);
        assertThat(result.seat().bookingDate()).isEqualTo("2026-09-21T08:15:30.000Z");
        assertThat(result.seat().bookingTime()).isEqualTo("2:15:30 PM");
        assertThat(result.seat().studentId()).isEqualTo("12345");
    }
    @Test void bulkDistinguishesOmittedStatusFromExplicitNull() throws Exception {
        SeatRepository repository = mock(SeatRepository.class);
        when(repository.update(anyString(), anyString(), any())).thenReturn(Optional.empty());
        BulkSeatUpdateRequest request = json.readValue("""
                {"seats":[{"seatNo":"01"},{"seatNo":"02","bookingStatus":null}]}
                """, BulkSeatUpdateRequest.class);
        var result = service(repository).updateMany("TRIP1", request);
        var captured = ArgumentCaptor.forClass(SeatChanges.class);
        verify(repository, times(2)).update(eq("TRIP1"), anyString(), captured.capture());
        assertThat(captured.getAllValues().get(0).statusProvided()).isFalse();
        assertThat(captured.getAllValues().get(1).statusProvided()).isTrue();
        assertThat(captured.getAllValues().get(1).bookingStatus()).isNull();
        assertThat(captured.getAllValues()).allMatch(c -> !c.replaceBookingDetails());
        assertThat(result.seats()).containsExactly(null, null);
    }
}
