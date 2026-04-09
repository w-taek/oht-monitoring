package com.example.ohtmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OhtmonApplication {

	public static void main(String[] args) {
		SpringApplication.run(OhtmonApplication.class, args);
	}

}
