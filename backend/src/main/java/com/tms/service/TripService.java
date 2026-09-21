package com.tms.service;

import com.tms.dto.request.TripRequest;
import com.tms.dto.response.*;
import java.util.List;

public interface TripService {
    CreateTripResponse create(TripRequest request);
    List<TripResponse> findAll();
    TripResponse findById(String id);
    UpdateTripResponse update(String id, TripRequest request);
    MessageResponse delete(String id);
}
