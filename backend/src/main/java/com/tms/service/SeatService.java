package com.tms.service;

import com.tms.dto.request.*;
import com.tms.dto.response.*;
import java.util.List;

public interface SeatService {
    SeatInitializationResponse initialize(String tripId);
    List<SeatResponse> findAll(String tripId);
    List<SeatResponse> findBooked(String tripId);
    List<SeatResponse> findByStudentId(String tripId, String studentId);
    SeatResponse findBySeatNo(String tripId, String seatNo);
    SeatUpdateResponse update(String tripId, String seatNo, SeatUpdateRequest request);
    BulkSeatUpdateResponse updateMany(String tripId, BulkSeatUpdateRequest request);
}
