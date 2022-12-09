package com.cs209a.spring_project.utils;

import java.sql.SQLException;

public class Test {

  public static void main(String[] args) throws SQLException {
    String[] url = "https://api.github.com/repos/spring-projects/spring-framework/releases/84106161".substring(
        29).split("/");
    String repo = url[0] + "/" + url[1];
    String str = "ddf\r\n\r\nddd'fm ''df";
  }
}
