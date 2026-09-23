package com.tms.service;

import com.tms.dto.request.Requests.*;
import com.tms.dto.response.Responses.*;
import java.util.*;

public interface CatalogService {
  List<StopView> stops(boolean all);

  StopView saveStop(UUID id, StopInput input);

  List<BusView> buses();

  BusView saveBus(UUID id, BusInput input);

  List<RouteView> routes();

  RouteView saveRoute(UUID id, RouteInput input);
}
