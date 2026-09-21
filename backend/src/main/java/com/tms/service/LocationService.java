package com.tms.service;

import com.tms.dto.request.LocationRequest;
import com.tms.dto.response.*;
import java.util.List;

public interface LocationService {
    CreateLocationResponse create(LocationRequest request);
    List<LocationResponse> findAll();
    LocationResponse findById(String id);
    UpdateLocationResponse update(String id, LocationRequest request);
    MessageResponse delete(String id);
}
