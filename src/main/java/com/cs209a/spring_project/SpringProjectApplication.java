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
	public static void main(String[] args) {
		SpringApplication.run(SpringProjectApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		crawler.initDatabase();


	}
}
