package com.tms.catalog.entity;

import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "route_stops")
public class RouteStop extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_id")
  private Route route;

  public Route getRoute() {
    return route;
  }

  public void setRoute(Route value) {
    route = value;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "stop_id")
  private Stop stop;

  public Stop getStop() {
    return stop;
  }

  public void setStop(Stop value) {
    stop = value;
  }

  private int sequence;

  public int getSequence() {
    return sequence;
  }

  public void setSequence(int value) {
    sequence = value;
  }
}
