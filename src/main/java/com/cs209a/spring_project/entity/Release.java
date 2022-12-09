package com.cs209a.spring_project.entity;

import java.util.Date;

public class Release {

  public int id;
  public String name;
  public Date created_time;
  String repo;
  public Release(int id, String tagName, Date created_time, String repository) {
    this.id = id;
    this.name = tagName;
    this.created_time = created_time;
    this.repo = repository;
  }

  @Override
  public String toString() {
    return String.format("id: %d\nname: %s\ndate:%s", id, name, created_time.toString());
  }
}
