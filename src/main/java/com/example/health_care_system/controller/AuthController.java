package com.example.health_care_system.controller;

import com.example.health_care_system.dto.LoginRequest;
import com.example.health_care_system.dto.RegisterRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.exception.AuthenticationException;
import com.example.health_care_system.exception.DuplicateResourceException;
import com.example.health_care_system.exception.ValidationException;
import com.example.health_care_system.service.AuthenticationService;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;
    
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            userService.registerPatient(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
            log.info("User registered successfully with email: {}", request.getEmail());
            return "redirect:/login";
        } catch (DuplicateResourceException e) {
            log.warn("Registration failed - duplicate email: {}", request.getEmail());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        } catch (ValidationException e) {
            log.warn("Registration failed - validation error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/register";
        }
    }
    
    @GetMapping("/login")
    public String showLoginPage(Model model, HttpSession session) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                       BindingResult result,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "login";
        }
        
        try {
            UserDTO user = authenticationService.authenticate(request);
            session.setAttribute("user", user);
            log.info("User logged in successfully: {}", user.getEmail());
            return "redirect:/dashboard";
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {}", request.getEmail());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Unexpected error during login", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Logged out successfully!");
        return "redirect:/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
}
