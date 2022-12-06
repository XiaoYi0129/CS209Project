package com.cs209a.spring_project.utils;

public class Helper {

  public static String formatTime(String timeData) {
    return formatString(timeData.replaceAll("T|Z", " "));
  }

  public static String getRepo(String url) {
    String[] strings = url.substring(29).split("/");
    return strings[0] + "/" + strings[1];
  }

  public static String formatString(String string) {
    if (string == null) {
      return null;
    }
    return "'" + string.replaceAll("\r|\n", "") + "'";
  }

}
