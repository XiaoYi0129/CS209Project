package com.cs209a.spring_project.entity;

import java.util.Date;

public class Release {
  public int id;

  public String name;

  public Date create_time;

  @Override
  public String toString() {
    return String.format("id: %d\nname: %s\ndate:%s", id, name, create_time.toString());
  }
}
