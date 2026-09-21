package com.tms.mapper;

import com.tms.entity.Seat;
import com.tms.dto.response.SeatResponse;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {
    public SeatResponse toResponse(Seat entity) {
        if (entity == null) return null;
        return new SeatResponse(entity.id(), entity.seatNo(), entity.bookingStatus(), entity.studentId(), entity.studentMail(), ResponseDates.format(entity.bookingDate()), entity.bookingTime(), entity.version());
    }
}
