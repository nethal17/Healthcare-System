package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Doctor;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class UserManagementController {
    
    private final UserService userService;
    
    @GetMapping("/users")
    public String viewUsers(
            @RequestParam(required = false) String role,
            HttpSession session,
            Model model) {
        
        // Check if user is logged in and is an admin
        UserDTO currentUser = (UserDTO) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/dashboard";
        }
        
        // Get users based on filter
        List<Patient> patients = null;
        List<Doctor> doctors = null;
        List<User> staff = null;
        
        if (role == null || role.equals("ALL")) {
            patients = userService.getAllPatients();
            doctors = userService.getAllDoctors();
            staff = userService.getUsersByRole(UserRole.STAFF);
        } else if (role.equals("PATIENT")) {
            patients = userService.getAllPatients();
        } else if (role.equals("DOCTOR")) {
            doctors = userService.getAllDoctors();
        } else if (role.equals("STAFF")) {
            staff = userService.getUsersByRole(UserRole.STAFF);
        }
        
        // Calculate statistics
        int totalUsers = userService.getTotalUserCount();
        int totalPatients = userService.getPatientCount();
        int totalDoctors = userService.getDoctorCount();
        int totalStaff = userService.getStaffCount();
        
        model.addAttribute("user", currentUser);
        model.addAttribute("patients", patients);
        model.addAttribute("doctors", doctors);
        model.addAttribute("staff", staff);
        model.addAttribute("selectedRole", role != null ? role : "ALL");
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalPatients", totalPatients);
        model.addAttribute("totalDoctors", totalDoctors);
        model.addAttribute("totalStaff", totalStaff);
        
        return "admin/users";
    }
}
