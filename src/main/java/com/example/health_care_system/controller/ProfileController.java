package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UpdateProfileRequest;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    
    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Refresh user data from database
        UserDTO refreshedUser = userService.getUserById(user.getId());
        session.setAttribute("user", refreshedUser);
        
        model.addAttribute("user", refreshedUser);
        
        // Create update request object for the form
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setName(refreshedUser.getName());
        updateRequest.setEmail(refreshedUser.getEmail());
        updateRequest.setContactNumber(refreshedUser.getContactNumber());
        updateRequest.setGender(refreshedUser.getGender());
        updateRequest.setDateOfBirth(refreshedUser.getDateOfBirth());
        updateRequest.setAddress(refreshedUser.getAddress());
        
        model.addAttribute("updateRequest", updateRequest);
        
        return "profile";
    }
    
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("updateRequest") UpdateProfileRequest request,
                               BindingResult result,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "profile";
        }
        
        try {
            UserDTO updatedUser = userService.updateProfile(user.getId(), request);
            session.setAttribute("user", updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match!");
            return "redirect:/profile";
        }
        
        // Validate password length
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters long!");
            return "redirect:/profile";
        }
        
        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
}
