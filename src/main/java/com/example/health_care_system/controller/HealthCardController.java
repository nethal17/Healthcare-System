package com.example.health_care_system.controller;

import com.example.health_care_system.dto.HealthCardDTO;
import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.HealthCard;
import com.example.health_care_system.service.HealthCardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/health-card")
@RequiredArgsConstructor
@Slf4j
public class HealthCardController {
    
    private final HealthCardService healthCardService;
    
    /**
     * View health card details
     */
    @GetMapping
    public String viewHealthCard(HttpSession session, Model model) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Only patients can view health cards
        if (!"PATIENT".equals(user.getRole().name())) {
            return "redirect:/dashboard";
        }
        
        Optional<HealthCard> healthCard = healthCardService.getHealthCardByPatientId(user.getId());
        if (healthCard.isPresent()) {
            HealthCardDTO healthCardDTO = healthCardService.convertToDTO(healthCard.get());
            model.addAttribute("healthCard", healthCardDTO);
            model.addAttribute("user", user);
        } else {
            model.addAttribute("error", "Health card not found");
        }
        
        return "health-card/view";
    }
    
    /**
     * Download health card as PNG image
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadHealthCard(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Only patients can download health cards
        if (!"PATIENT".equals(user.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            Optional<HealthCard> healthCard = healthCardService.getHealthCardByPatientId(user.getId());
            if (healthCard.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageBytes = healthCardService.generateHealthCardImage(healthCard.get());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "health-card-" + user.getName().replaceAll(" ", "_") + ".png");
            headers.setContentLength(imageBytes.length);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Error generating health card image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download health card by ID (for admin/staff)
     */
    @GetMapping("/download/{healthCardId}")
    public ResponseEntity<byte[]> downloadHealthCardById(@PathVariable String healthCardId, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            Optional<HealthCard> healthCard = healthCardService.getHealthCardById(healthCardId);
            if (healthCard.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] imageBytes = healthCardService.generateHealthCardImage(healthCard.get());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "health-card-" + healthCard.get().getPatientName().replaceAll(" ", "_") + ".png");
            headers.setContentLength(imageBytes.length);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Error generating health card image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
