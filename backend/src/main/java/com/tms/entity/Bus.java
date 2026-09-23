package com.tms.entity;

import com.tms.entity.Types.*;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "buses")
public class Bus extends BaseEntity {
  private String registration;

  public String getRegistration() {
    return registration;
  }

  public void setRegistration(String value) {
    registration = value;
  }

  private String busType;

  public String getBusType() {
    return busType;
  }

  public void setBusType(String value) {
    busType = value;
  }

  private boolean active = true;

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    active = value;
  }
}
