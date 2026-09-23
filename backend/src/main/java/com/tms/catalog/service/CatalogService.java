package com.tms.catalog.service;

import com.tms.catalog.dto.request.BusInput;
import com.tms.catalog.dto.request.RouteInput;
import com.tms.catalog.dto.request.StopInput;
import com.tms.catalog.dto.response.BusView;
import com.tms.catalog.dto.response.RouteView;
import com.tms.catalog.dto.response.StopView;
import java.util.*;

public interface CatalogService {
  List<StopView> stops(boolean all);

  StopView saveStop(UUID id, StopInput input);

  List<BusView> buses();

  BusView saveBus(UUID id, BusInput input);

  List<RouteView> routes();

  RouteView saveRoute(UUID id, RouteInput input);
}
