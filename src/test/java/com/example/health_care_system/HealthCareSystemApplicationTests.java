package com.example.health_care_system;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

class HealthCareSystemApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void negative_numberFormat_throws() {
		assertThrows(NumberFormatException.class, () -> Integer.parseInt("not-a-number"));
	}

	@Test
	void negative_listIndexOutOfBounds_throws() {
		java.util.List<String> list = java.util.List.of();
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
	}

}
