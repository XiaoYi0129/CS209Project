package com.cs209a.spring_project.entity;

import java.util.Date;
import java.util.List;

public class Issue {
  int id;
  String state;
  Date createdTime;
  Date closedTime;
  String title;
  String body;
  String repo;
  List<Comment> comments;



}
