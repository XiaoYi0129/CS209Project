package com.example.springproject;

import java.util.Date;

public class Release {
  int id;

  String name;

  Date create_time;

  @Override
  public String toString() {
    return String.format("id: %d\nname: %s\ndate:%s", id, name, create_time.toString());
  }
}
