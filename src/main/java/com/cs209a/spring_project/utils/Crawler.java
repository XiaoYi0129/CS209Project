package com.cs209a.spring_project.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

@Component
public class Crawler {

  //github api似乎限制访问次数60次/小时，要找小一点的库或者分几次跑
  public static String GITHUB_API = "https://api.github.com/repos/%s/%s?page=%d&per_page=100";
  public static String[] REPO_LIST = {"jhy/jsoup", "rubenlagus/TelegramBots"};
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
    int i = 0;
    String data;

    while (true) {
      try {
        String urlString = String.format(GITHUB_API, repoName, request, ++i);
        if (request.equals("issues")) {
          urlString += "&state=all";
        }
        HttpURLConnection conn = Helper.getConnection(urlString);
        assert conn != null;
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

          System.out.println("parsing json of page " + i + "...");
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
          return;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void addComments(String commentsUrl, int issueId) {
    String data;
    try {
      HttpURLConnection conn = Helper.getConnection(commentsUrl);
      assert conn != null;
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
          String body = (String) jsonObject.get("body");

          JSONObject user = (JSONObject) jsonObject.get("user");
          String userName = (String) user.get("login");
          Integer userId = (Integer) user.get("id");
          //插入developer
          jdbcTemplate.execute("REPLACE INTO `developer` VALUES (?, ?);",
              (PreparedStatementCallback<Boolean>) ps -> {
                ps.setInt(1, userId);
                ps.setString(2, userName);
                return ps.execute();
              });
          //插入comment
          jdbcTemplate.execute("REPLACE INTO comment VALUES (?, ?, ?, ?)",
              (PreparedStatementCallback<Boolean>) ps -> {
                ps.setInt(1, id);
                ps.setInt(2, issueId);
                ps.setInt(3, userId);
                ps.setString(4, body);
                return ps.execute();
              });
        }
      } else {
        System.out.println("Network issue: " + responseCode);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(commentsUrl);
    }
  }

  private void handleContributors(JSONObject jsonObject, String repoName) {
    Integer id = (Integer) jsonObject.get("id");
    String name = (String) jsonObject.get("login");
    Integer contributions = (Integer) jsonObject.get("contributions");

    jdbcTemplate.execute(
        "REPLACE INTO `developer` VALUES (?, ?);", (PreparedStatementCallback<Boolean>) ps -> {
          ps.setInt(1, id);
          ps.setString(2, name);
          return ps.execute();
        });
    jdbcTemplate.execute(
        "REPLACE INTO repository VALUES (?, ?, ?);", (PreparedStatementCallback<Boolean>) ps -> {
          ps.setString(1, repoName);
          ps.setInt(2, id);
          ps.setInt(3, contributions);
          return ps.execute();
        });
  }

  private void handleIssues(JSONObject jsonObject) {
    String repo = Helper.getRepo((String) jsonObject.get("url"));
    Integer id = (Integer) jsonObject.get("id");
    String title = (String) jsonObject.get("title");
    String state = (String) jsonObject.get("state");
    String createdTime = Helper.formatTime((String) jsonObject.get("created_at"));
    String closedTime = Helper.formatTime((String) jsonObject.get("closed_at"));
    String body = (String) jsonObject.get("body");

    jdbcTemplate.execute("REPLACE INTO issue VALUES (?, ?, ?, ?, ?, ?, ?);",
        (PreparedStatementCallback<Boolean>) ps -> {
          ps.setInt(1, id);
          ps.setString(2, state);
          ps.setString(3, createdTime);
          ps.setString(4, closedTime);
          ps.setString(5, title);
          ps.setString(6, body);
          ps.setString(7, repo);
          return ps.execute();
        });
    //添加comments
    String commentsUrl = (String) jsonObject.get("comments_url");
    System.out.println("parsing " + commentsUrl);
    addComments(commentsUrl, id);
  }

  private void handleReleases(JSONObject jsonObject) {
    Integer id = (Integer) jsonObject.get("id");
    String name = (String) jsonObject.get("tag_name");
    String time = Helper.formatTime((String) jsonObject.get("created_at"));
    String repo = Helper.getRepo((String) jsonObject.get("url"));

    jdbcTemplate.execute(
        "REPLACE INTO `release` VALUES (?, ?, ?, ?);", (PreparedStatementCallback<Boolean>) ps -> {
          ps.setInt(1, id);
          ps.setString(2, name);
          ps.setString(3, time);
          ps.setString(4, repo);
          return ps.execute();
        });
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
        "REPLACE INTO `commit` VALUES (?, ?, ?);", (PreparedStatementCallback<Boolean>) ps -> {
          ps.setString(1, sha);
          ps.setString(2, date);
          ps.setString(3, repo);
          return ps.execute();
        });
  }

  public void Test() {
    int id = 2;
    String state = "close";
    String createdTime = "2021-12-13 12:34:57";
    String closedTime = null;
    String title = "ssdda";
    String body = null;
    String repo = "repo";
//    jdbcTemplate.execute(
//        "REPLACE INTO issue VALUES (?, ?, ?,?,?,?,?);", (PreparedStatementCallback<Boolean>) ps  -> {
//          ps.setInt(1, id);
//          ps.setString(2, state);
//          ps.setString(3, createdTime);
//          ps.setString(4, closedTime);
//          ps.setString(5, title);
//          ps.setString(6, body);
//          ps.setString(7, repo);
//          return ps.execute();
//        });

//            2, "'close'", "'2021-12-13 12:34:57'", null, "'title'", "'body'", "'repo'"));
  }

}
