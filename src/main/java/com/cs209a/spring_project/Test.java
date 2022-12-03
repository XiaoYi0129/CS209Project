package com.cs209a.spring_project;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Test {

  public static void main(String[] args) {

    getReleaseNum("spring-projects", "spring-framework");
//    getCommitNum("spring-projects", "spring-framework");
  }

  public static void getReleaseNum(String owner, String repo) {
    String data = "";
    String url = String.format("https://api.github.com/repos/%s/%s/releases/per_page/100", owner, repo);
    url = "https://api.github.com/repos/spring-projects/spring-framework/releases?per_page=100&page=1";
    try {
      URL restUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) restUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);


      int responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        data = reader.readLine();
      }

      JSONArray jsonArray = (JSONArray) JSONArray.parse(data);
      System.out.println(jsonArray.size());
      jsonArray.forEach(o -> {
        JSONObject release = (JSONObject) o;
        Integer id = (Integer) release.get("id");
        String name = (String) release.get("tag_name");
        String time = (String) release.get("created_at");

        System.out.printf("\nid: %d\nname: %s\ntime: %s\n", id, name, time);
      });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void getCommitNum(String owner, String repo) {
    String data = "";
    String url = String.format("https://api.github.com/repos/%s/%s/commits", owner, repo);
    try {
      URL restUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) restUrl.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);

      int responseCode = connection.getResponseCode();
      if (responseCode == 200) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        data = reader.readLine();
      }

      JSONArray jsonArray = (JSONArray) JSONArray.parse(data);
      System.out.println(jsonArray.size());
      jsonArray.forEach(o -> {
        JSONObject commitObject = (JSONObject) o;
        String sha = (String) commitObject.get("sha");

        JSONObject commit = (JSONObject) commitObject.get("commit");
        JSONObject author = (JSONObject) commit.get("author");
        String date = (String) author.get("date");

        System.out.printf("\nsha: %s\ndate: %s\n", sha, date);
      });

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
