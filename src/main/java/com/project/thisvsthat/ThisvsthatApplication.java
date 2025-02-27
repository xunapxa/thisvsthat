package com.project.thisvsthat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ThisvsthatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThisvsthatApplication.class, args);
	}

}
