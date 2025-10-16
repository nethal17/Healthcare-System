package com.example.health_care_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@SpringBootApplication
@Controller
@EnableScheduling
public class HealthCareSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthCareSystemApplication.class, args);
	}

}
