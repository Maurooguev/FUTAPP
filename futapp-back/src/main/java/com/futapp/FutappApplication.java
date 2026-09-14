package com.futapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FutappApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutappApplication.class, args);
	}

}
