package com.cs209a.spring_project.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class Helper {

  //改成自己的token
  static String TOKEN = "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";

  public static String formatTime(String timeData) {
    if (timeData == null) {
      return null;
    }
    return timeData.replaceAll("T|Z", " ");
  }

  public static String getRepo(String url) {
    String[] strings = url.substring(29).split("/");
    return strings[0] + "/" + strings[1];
  }

  public static HttpURLConnection getConnection(String urlString) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("Authorization", "Bearer " + TOKEN);
      conn.setRequestMethod("GET");
      return conn;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
