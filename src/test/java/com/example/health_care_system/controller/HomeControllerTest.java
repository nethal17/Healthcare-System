package com.example.health_care_system.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {

    private final HomeController controller = new HomeController();

    @Test
    void home_returnsHomeView() {
        assertEquals("home", controller.home());
    }

    @Test
    void homePage_returnsHomeView() {
        assertEquals("home", controller.homePage());
    }
}
