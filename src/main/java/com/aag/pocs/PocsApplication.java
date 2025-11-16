package com.aag.pocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PocsApplication {

	public static void main(String[] args) {
		SpringApplication.run(PocsApplication.class, args);

	}
}
