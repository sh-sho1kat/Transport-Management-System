package com.tms.repository;

import com.tms.entity.Seat;
import com.tms.entity.SeatChanges;
import java.util.List;
import java.util.Optional;

public interface SeatRepository {
    void replaceAll(String tripId, List<Seat> seats);
    List<Seat> findAll(String tripId);
    List<Seat> findBooked(String tripId);
    List<Seat> findByStudentId(String tripId, String studentId);
    Optional<Seat> findBySeatNo(String tripId, String seatNo);
    Optional<Seat> update(String tripId, String seatNo, SeatChanges changes);
}
