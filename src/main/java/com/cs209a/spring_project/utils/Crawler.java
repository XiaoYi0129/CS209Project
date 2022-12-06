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

  //github api似乎限制访问次数60次/小时，要找小一点的库或者分几次跑
  public static String GITHUB_API = "https://api.github.com/repos/%s/%s?page=%d&per_page=100";
  public static String[] REPO_LIST = {"jhy/jsoup", "junit-team/junit5"};
  public static String[] REQUEST_LIST = {"contributors", "releases", "commits", "issues"};
  @Autowired
  JdbcTemplate jdbcTemplate;

  /**
   * 执行建表的sql语句
   */
  public void initDatabase() {
    jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS CS209A;");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `repository`("
        + "    `name` varchar(80) not null,"
        + "    `contributor` int unsigned not null,"
        + "    `contributions` int unsigned not null,"
        + "    primary key (`name`, `contributor`)"
        + ");");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `developer`("
        + "    `id` int unsigned not null,"
        + "    `name` varchar(50) not null,"
        + "    primary key (id)\n"
        + ");");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `issue`("
        + "    `id` int unsigned not null,"
        + "    `state` varchar(10) not null,"
        + "    `created_time` datetime not null,"
        + "    `closed_time` datetime,"
        + "    `title` varchar(150) not null,"
        + "    `body` varchar(500) not null,"
        + "    `repository` varchar(80) not null,"
        + "    primary key (id)"
        + ");");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `comment`("
        + "    `id` int unsigned not null,"
        + "    `user` int unsigned not null,"
        + "    `issue` int unsigned not null,"
        + "    `body` varchar(500) not null,"
        + "    primary key (id)"
        + ");");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `release`("
        + "    `id` int unsigned not null,"
        + "    `name` varchar(30) not null,"
        + "    `created_time` datetime not null,"
        + "    `repository` varchar(80) not null,"
        + "    primary key (id)"
        + ");");
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `commit`("
        + "    `id` varchar(50) not null,"
        + "    `date` datetime,"
        + "    primary key (id)"
        + ");");

  }

  public void addRepoData() {
    for (String repoName : REPO_LIST) {
      for (String request : REQUEST_LIST) {
        System.out.printf("getting %s %s\n", repoName, request);
        addData(repoName, request);
      }
    }
  }

  public void addData(String repoName, String request) {
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
          if (data.equals("[]")) {
            break;
          }

          System.out.println("parsing json of page " + (i - 1) + "...");
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
          System.out.println("Network issue: " + responseCode);
          break;
        }

      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  public void addComments(String commentsUrl, int issueId) {
    String data;
    try {
      URL url = new URL(commentsUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      int responseCode = conn.getResponseCode();

      if (responseCode == 200) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        data = reader.readLine();
        // data为空结束循环
        if (data.equals("[]")) {
          return;
        }

        JSONArray jsonArray = JSONArray.parseArray(data);
        for (Object object : jsonArray) {
          JSONObject jsonObject = (JSONObject) object;
          Integer id = (Integer) jsonObject.get("id");
          String body = Helper.formatString((String) jsonObject.get("body"));

          JSONObject user = (JSONObject) jsonObject.get("user");
          String userName = (String) user.get("login");
          Integer userId = (Integer) user.get("id");
          //插入developer
          jdbcTemplate.execute(String.format("REPLACE INTO `developer` VALUES (%d, '%s');",
              userId, userName));
          //插入comment
          jdbcTemplate.execute(
              String.format("REPLACE INTO comment VALUES (%d, %d, %d, %s)", id, issueId, userId,
                  body));
        }

      } else {
        System.out.println("Network issue: " + responseCode);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleContributors(JSONObject jsonObject, String repoName) {
    Integer id = (Integer) jsonObject.get("id");
    String name = (String) jsonObject.get("login");
    Integer contributions = (Integer) jsonObject.get("contributions");

    jdbcTemplate.execute(
        String.format("REPLACE INTO `developer` VALUES (%d, '%s');",
            id, name));
    jdbcTemplate.execute(
        String.format("REPLACE INTO repository VALUES ('%s', %d, %d);", repoName, id,
            contributions));
  }

  private void handleIssues(JSONObject jsonObject) {
    //比较难搞，最后再写
    String repo = Helper.getRepo((String) jsonObject.get("url"));
    Integer id = (Integer) jsonObject.get("id");
    String title = Helper.formatString((String) jsonObject.get("title"));
    String state = Helper.formatString((String) jsonObject.get("state"));
    String createdTime = Helper.formatTime((String) jsonObject.get("created_at"));
    String closedTime = Helper.formatTime((String) jsonObject.get("closed_at"));
    String body = Helper.formatString((String) jsonObject.get("body"));

    jdbcTemplate.execute(
        String.format("REPLACE INTO issue VALUES (%d, %s, %s, %s, %s, %s, %s);", id, state,
            createdTime, closedTime, title, body, repo));

    String commentsUrl = (String) jsonObject.get("comments_url");
    addComments(commentsUrl, id);

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
    String repo = Helper.getRepo((String) jsonObject.get("url"));
    //获取date
    JSONObject commit = (JSONObject) jsonObject.get("commit");
    JSONObject author = (JSONObject) commit.get("author");
    String date = Helper.formatTime((String) author.get("date"));
    // 执行插入语句
    jdbcTemplate.execute(
        String.format("REPLACE INTO `commit` VALUES ('%s', %s, '%s');",
            sha, date, repo));
  }

  public void Test() {
    jdbcTemplate.execute(
        String.format("REPLACE INTO issue VALUES (%d, %s, %s,%s,%s,%s,%s);",
            2, "'close'", "'2021-12-13 12:34:57'", null, "'title'", "'body'", "'repo'"));
  }

}
