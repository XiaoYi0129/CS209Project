package com.cs209a.spring_project;

import com.cs209a.spring_project.entity.Release;
import java.util.List;
import javax.sql.DataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DatabaseTest {
  @Autowired
  DataSource dataSource;
  @Autowired
  JdbcTemplate jdbcTemplate;

  @Test
  public void contextLoads() throws Exception{
    System.out.println(dataSource.getConnection());

    String sql = "select * from release_test";
    List<Release> releaseList = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Release release = new Release();
      release.id = rs.getInt("id");
      release.name = rs.getString("name");
      release.create_time = rs.getTimestamp("create_time");
      return release;
    });

    for (Release release: releaseList) {
      System.out.println(release);
    }

  }
}
