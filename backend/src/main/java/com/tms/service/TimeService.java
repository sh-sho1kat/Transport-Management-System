package com.tms.service;

import com.tms.dto.request.TimeRequest;
import com.tms.dto.response.*;
import java.util.List;

public interface TimeService {
    CreateTimeResponse create(TimeRequest request);
    List<TimeResponse> findAll();
    TimeResponse findById(String id);
    UpdateTimeResponse update(String id, TimeRequest request);
    MessageResponse delete(String id);
}
