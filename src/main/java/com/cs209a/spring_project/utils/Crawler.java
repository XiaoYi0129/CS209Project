package com.cs209a.spring_project.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Crawler {
  //github api似乎限制访问次数60次/小时，要找小一点的库
  static String GITHUB_API = "https://api.github.com/repos/%s/%s?page=%d&per_page=100";
  static String[] REPO_LIST = {"jhy/jsoup", "junit-team/junit5"};
  static String[] REQUEST_LIST = {"contributors", "releases", "commits"};
  @Autowired
  JdbcTemplate jdbcTemplate;

  /**
   * 执行建表的sql语句
   */
  public void initDatabase() {
    jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS CS209A;");
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS contributor(id int unsigned not null, name varchar(50) not null, contribution int unsigned not null, repository varchar(80) not null, primary key (repository, id));");
    //其他建表语句
    //    jdbcTemplate.execute();
  }

  public void getRepoData() {
    for (String repoName : REPO_LIST) {
      for (String request : REQUEST_LIST) {
        System.out.printf("getting %s %s\n", repoName, request);
        getData(repoName, request);
      }
    }
    System.out.println("data collection done!");
  }

  private void getData(String repoName, String request) {
    int i = 1;
    String data;

    while (true) {
      try {
        URL url = new URL(String.format(GITHUB_API, repoName, request, i++));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
          data = reader.readLine();
          // data为空结束循环
          if (data.equals("[]") ) {
//            System.out.printf("get %s %s done\n", repoName, request);
            break;
          }

          if (data.length() < 10) {
            System.out.println("data: "+ data);
          }

          System.out.println("parsing json...");

          JSONArray jsonArray = JSONArray.parseArray(data);
          for (Object object : jsonArray) {
            JSONObject jsonObject = (JSONObject) object;
            switch (request) {
              case "contributors":
                handleContributors(jsonObject, repoName);
                break;
              case "issues":
                handleIssues(jsonObject);
                break;
              case "releases":
                handleReleases(jsonObject);
                break;
              case "commits":
                handleCommits(jsonObject);
                break;
            }
          }


        } else {
          System.out.println("Network issue: "+ responseCode);
          break;
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  private void handleContributors(JSONObject jsonObject, String repoName) {
    Integer id = (Integer) jsonObject.get("id");
    String name = (String) jsonObject.get("login");
    Integer contributions = (Integer) jsonObject.get("contributions");

    jdbcTemplate.execute(
        String.format("INSERT INTO `developer` VALUES (%d, '%s') ON DUPLICATE KEY UPDATE id = id;",
            id, name));
    jdbcTemplate.execute(
        String.format("REPLACE INTO repository VALUES ('%s', %d, %d);", repoName, id, contributions));
  }

  private void handleIssues(JSONObject jsonObject) {
    //比较难搞，最后再写
  }

  private void handleReleases(JSONObject jsonObject) {
    Integer id = (Integer) jsonObject.get("id");
    String name = (String) jsonObject.get("tag_name");
    String time = Helper.formatTime((String) jsonObject.get("created_at"));
    String repo = Helper.getRepo((String) jsonObject.get("url"));

    jdbcTemplate.execute(String.format(
        "REPLACE INTO `release` VALUES (%d, '%s', '%s', '%s');", id,
        name, time, repo));
  }

  private void handleCommits(JSONObject jsonObject) {
    String sha = (String) jsonObject.get("sha");
    //获取date
    JSONObject commit = (JSONObject) jsonObject.get("commit");
    JSONObject author = (JSONObject) commit.get("author");
    String date = Helper.formatTime((String) author.get("date"));
    // 执行插入语句
    jdbcTemplate.execute(
        String.format("REPLACE INTO `commit` VALUES ('%s', '%s');",
            sha, date));
  }

  public void Test() {
    jdbcTemplate.execute(
        String.format("REPLACE INTO repository VALUES ('%s', %d, %d);",
            "asada", 2, 134));
  }

}
