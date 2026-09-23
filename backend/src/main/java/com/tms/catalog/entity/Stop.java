package com.tms.catalog.entity;

import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "stops")
public class Stop extends BaseEntity {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  private String city;

  public String getCity() {
    return city;
  }

  public void setCity(String value) {
    city = value;
  }

  private String address;

  public String getAddress() {
    return address;
  }

  public void setAddress(String value) {
    address = value;
  }

  private boolean active = true;

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    active = value;
  }
}
