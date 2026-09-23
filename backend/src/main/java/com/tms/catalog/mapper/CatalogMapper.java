package com.tms.catalog.mapper;

import com.tms.catalog.dto.response.BusView;
import com.tms.catalog.dto.response.Layout;
import com.tms.catalog.dto.response.RouteView;
import com.tms.catalog.dto.response.StopView;
import com.tms.catalog.entity.Bus;
import com.tms.catalog.entity.BusSeat;
import com.tms.catalog.entity.Route;
import com.tms.catalog.entity.RouteStop;
import com.tms.catalog.entity.Stop;
import java.util.*;

public final class CatalogMapper {
  private CatalogMapper() {}

  public static StopView stop(Stop s) {
    return new StopView(s.getId(), s.getName(), s.getCity(), s.getAddress(), s.getActive());
  }

  public static BusView bus(Bus b, List<BusSeat> seats) {
    return new BusView(
        b.getId(),
        b.getRegistration(),
        b.getBusType(),
        b.getActive(),
        seats.stream()
            .map(
                s ->
                    new Layout(s.getLabel(), s.getRowNumber(), s.getColumnNumber(), s.getBlocked()))
            .toList());
  }

  public static RouteView route(Route r, List<RouteStop> stops) {
    return new RouteView(
        r.getId(),
        r.getCode(),
        r.getName(),
        r.getActive(),
        stops.stream().map(s -> stop(s.getStop())).toList());
  }
}
