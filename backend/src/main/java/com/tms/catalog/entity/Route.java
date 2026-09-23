package com.tms.catalog.entity;

import com.tms.shared.persistence.BaseEntity;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "routes")
public class Route extends BaseEntity {
  private String code;

  public String getCode() {
    return code;
  }

  public void setCode(String value) {
    code = value;
  }

  private String name;

  public String getName() {
    return name;
  }

  public void setName(String value) {
    name = value;
  }

  private boolean active = true;

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean value) {
    active = value;
  }
}
