package com.cs209a.spring_project.dao;

import javax.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ContributorDao{
  @Resource
  JdbcTemplate jdbcTemplate;

  public int getContributorCount(String repoName) {

    return 0;
  }

}
