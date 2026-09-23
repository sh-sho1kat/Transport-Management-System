package com.tms.catalog.entity;

import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "bus_seats")
public class BusSeat extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bus_id")
  private Bus bus;

  public Bus getBus() {
    return bus;
  }

  public void setBus(Bus value) {
    bus = value;
  }

  private String label;

  public String getLabel() {
    return label;
  }

  public void setLabel(String value) {
    label = value;
  }

  private int rowNumber;

  public int getRowNumber() {
    return rowNumber;
  }

  public void setRowNumber(int value) {
    rowNumber = value;
  }

  private int columnNumber;

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setColumnNumber(int value) {
    columnNumber = value;
  }

  private boolean blocked;

  public boolean getBlocked() {
    return blocked;
  }

  public void setBlocked(boolean value) {
    blocked = value;
  }
}
