package com.cs209a.spring_project;

import com.cs209a.spring_project.utils.Crawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringProjectApplication implements ApplicationRunner {
	@Autowired
	Crawler crawler;
	boolean update = true;
	public static void main(String[] args) {
		SpringApplication.run(SpringProjectApplication.class, args);
	}
	@Override
	public void run(ApplicationArguments args) throws Exception {
//		crawler.Test();
		if (update) {
			System.out.println("initializing database...");
//			crawler.initDatabase();
//			crawler.addRepoData();
			String repoName = Crawler.REPO_LIST[1];
			String request = Crawler.REQUEST_LIST[3];
			System.out.printf("getting %s %s\n", repoName, request);
			crawler.addData(repoName, request);
			System.out.println("data collection done!");
		}

	}
}
