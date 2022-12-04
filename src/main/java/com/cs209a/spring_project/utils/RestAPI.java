package com.cs209a.spring_project.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestAPI {

  public static void main(String[] args) {
    try {
      StringBuilder json = new StringBuilder();

      String s = "https://api.github.com/repos/alibaba/fastjson/contributors?page=2&per_page=100";
      URL url = new URL(s);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();

      int responseCode = conn.getResponseCode();
      String responseMessage = conn.getResponseMessage();
      String contentEncoding = conn.getContentEncoding();

      //读取该url返回的json数据

      BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),
          StandardCharsets.UTF_8));
      String inputLine = null;
      while ((inputLine = in.readLine()) != null) {
        json.append(inputLine);

      }
      //关闭输入流
      in.close();
//            String jsonString= in.readLine();
      System.out.println(json);//这里数据就不完整

      JSONArray jsonArray = JSONArray.parseArray(json.toString());
      System.out.println(jsonArray);

      for (Object i : jsonArray) {
        JSONObject jsonObject = (JSONObject) i;
        String login = (String) jsonObject.get("login");
        System.out.println(login);
      }
//只读到第26个

    } catch (MalformedURLException e) {

    } catch (IOException e) {

    }


  }
}