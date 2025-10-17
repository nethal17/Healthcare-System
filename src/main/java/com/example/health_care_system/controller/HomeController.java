package com.example.health_care_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    /**
     * Home page - Landing page for the application
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    /**
     * Explicit /home route
     */
    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}
