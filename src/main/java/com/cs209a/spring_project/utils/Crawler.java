package com.cs209a.spring_project.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Crawler {

  @Autowired
  JdbcTemplate jdbcTemplate;

  public void initDatabase() {
    jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS CS209A;");
    jdbcTemplate.execute(
        "CREATE TABLE IF NOT EXISTS contributor(id int unsigned not null, name varchar(50) not null, contribution int unsigned not null, repository varchar(80) not null, primary key (repository, id));");
//    jdbcTemplate.execute();
  }

}
