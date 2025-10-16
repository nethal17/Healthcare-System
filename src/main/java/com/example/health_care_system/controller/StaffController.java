package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.Patient;
import com.example.health_care_system.model.User;
import com.example.health_care_system.service.QRCodeService;
import com.example.health_care_system.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {
    
    private final UserService userService;
    private final QRCodeService qrCodeService;
    
    @GetMapping("/scanner")
    public String showScanner(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null || user.getRole().name() != "STAFF") {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "staff/scanner";
    }
    
    @PostMapping("/scan-qr")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanQRCode(@RequestBody Map<String, String> request, 
                                                          HttpSession session) {
        try {
            UserDTO staffUser = (UserDTO) session.getAttribute("user");
            if (staffUser == null || staffUser.getRole().name() != "STAFF") {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            
            String qrContent = request.get("qrContent");
            
            // Extract user ID from QR code
            String userId = qrCodeService.extractUserIdFromQRContent(qrContent);
            
            // Get user details
            User user = userService.getUserEntityById(userId);
            
            // Only patients have QR codes, so this should be a Patient
            if (!(user instanceof Patient)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code - not a patient"));
            }
            
            Patient patient = (Patient) user;
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", Map.of(
                "id", patient.getId(),
                "name", patient.getName(),
                "email", patient.getEmail(),
                "role", patient.getRole().name(),
                "dateOfBirth", patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "",
                "gender", patient.getGender() != null ? patient.getGender() : "",
                "address", patient.getAddress() != null ? patient.getAddress() : "",
                "contactNumber", patient.getContactNumber() != null ? patient.getContactNumber() : ""
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code format"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to scan QR code: " + e.getMessage()));
        }
    }
}
