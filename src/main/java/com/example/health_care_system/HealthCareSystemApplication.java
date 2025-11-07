package com.example.health_care_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Controller
@EnableScheduling
@EnableAsync
public class HealthCareSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthCareSystemApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
