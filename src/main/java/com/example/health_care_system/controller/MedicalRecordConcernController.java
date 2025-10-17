package com.example.health_care_system.controller;

import com.example.health_care_system.dto.UserDTO;
import com.example.health_care_system.model.MedicalRecordConcern;
import com.example.health_care_system.model.UserRole;
import com.example.health_care_system.service.MedicalRecordConcernService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/medical-record-concerns")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordConcernController {
    
    private final MedicalRecordConcernService concernService;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Submit a new concern about a medical record
     */
    @PostMapping("/submit")
    public String submitConcern(
            @RequestParam String medicalRecordId,
            @RequestParam String concernText,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            log.warn("Unauthorized access attempt to submit concern");
            redirectAttributes.addFlashAttribute("error", "Please login to submit a concern");
            return "redirect:/login";
        }
        
        if (user.getRole() != UserRole.PATIENT) {
            log.warn("Non-patient user attempting to submit concern");
            redirectAttributes.addFlashAttribute("error", "Only patients can submit concerns");
            return "redirect:/medical-records";
        }
        
        try {
            MedicalRecordConcern concern = new MedicalRecordConcern();
            concern.setMedicalRecordId(medicalRecordId);
            concern.setPatientId(user.getId());
            concern.setPatientName(user.getName());
            concern.setPatientEmail(user.getEmail());
            concern.setConcernText(concernText);
            
            concernService.createConcern(concern);
            
            log.info("Concern submitted successfully by patient: {}", user.getName());
            redirectAttributes.addFlashAttribute("success", "Your concern has been submitted successfully. The healthcare manager will review it shortly.");
        } catch (Exception e) {
            log.error("Error submitting concern: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to submit concern. Please try again.");
        }
        
        return "redirect:/medical-records";
    }
    
    /**
     * View all concerns (Admin only)
     */
    @GetMapping("/view")
    public String viewConcerns(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            log.warn("Unauthorized access attempt to view concerns");
            redirectAttributes.addFlashAttribute("error", "Please login to view concerns");
            return "redirect:/login";
        }
        
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("Non-admin user attempting to view concerns");
            redirectAttributes.addFlashAttribute("error", "Only healthcare managers can view concerns");
            return "redirect:/dashboard";
        }
        
        List<MedicalRecordConcern> concerns = concernService.getAllConcerns();
        
        // Calculate statistics
        long pendingCount = concerns.stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .count();
        long repliedCount = concerns.stream()
                .filter(c -> "REPLIED".equals(c.getStatus()))
                .count();
        
        model.addAttribute("concerns", concerns);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("repliedCount", repliedCount);
        model.addAttribute("user", user);
        
        log.info("Admin {} viewing {} concerns", user.getName(), concerns.size());
        
        return "view-concerns";
    }
    
    /**
     * Reply to a concern and send email
     */
    @PostMapping("/reply")
    public String replyConcern(
            @RequestParam String concernId,
            @RequestParam String replyText,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null) {
            log.warn("Unauthorized access attempt to reply to concern");
            redirectAttributes.addFlashAttribute("error", "Please login to reply to concerns");
            return "redirect:/login";
        }
        
        if (user.getRole() != UserRole.ADMIN) {
            log.warn("Non-admin user attempting to reply to concern");
            redirectAttributes.addFlashAttribute("error", "Only healthcare managers can reply to concerns");
            return "redirect:/dashboard";
        }
        
        try {
            // Update concern with reply
            MedicalRecordConcern concern = concernService.replyConcern(concernId, replyText, user.getName());
            
            // Send email to patient
            sendReplyEmail(concern);
            
            log.info("Reply sent successfully by admin: {}", user.getName());
            redirectAttributes.addFlashAttribute("success", "Reply sent successfully and email notification sent to the patient.");
        } catch (Exception e) {
            log.error("Error replying to concern: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to send reply. Please try again.");
        }
        
        return "redirect:/medical-record-concerns/view";
    }
    
    /**
     * Delete a concern
     */
    @PostMapping("/delete")
    public String deleteConcern(
            @RequestParam String concernId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        if (user == null || user.getRole() != UserRole.ADMIN) {
            log.warn("Unauthorized access attempt to delete concern");
            redirectAttributes.addFlashAttribute("error", "Unauthorized access");
            return "redirect:/login";
        }
        
        try {
            concernService.deleteConcern(concernId);
            log.info("Concern deleted successfully by admin: {}", user.getName());
            redirectAttributes.addFlashAttribute("success", "Concern deleted successfully.");
        } catch (Exception e) {
            log.error("Error deleting concern: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete concern. Please try again.");
        }
        
        return "redirect:/medical-record-concerns/view";
    }
    
    /**
     * Send email reply to patient
     */
    private void sendReplyEmail(MedicalRecordConcern concern) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom("company.healthcare25@gmail.com");
        helper.setTo(concern.getPatientEmail());
        helper.setSubject("Response to Your Medical Record Concern - Healthcare System");
        
        String htmlContent = buildEmailContent(concern);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        log.info("Email sent to patient: {} at {}", concern.getPatientName(), concern.getPatientEmail());
    }
    
    /**
     * Build HTML email content
     */
    private String buildEmailContent(MedicalRecordConcern concern) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #3b82f6; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                    .concern-box { background-color: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .reply-box { background-color: #dbeafe; border-left: 4px solid #3b82f6; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { background-color: #1f2937; color: white; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 14px; }
                    h1 { margin: 0; font-size: 24px; }
                    h3 { color: #1f2937; margin-top: 0; }
                    .label { font-weight: bold; color: #4b5563; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🏥 Healthcare System</h1>
                        <p style="margin: 5px 0 0 0;">Medical Record Concern Response</p>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Thank you for reaching out to us regarding your medical record concern. We have reviewed your concern and are providing the following response:</p>
                        
                        <div class="concern-box">
                            <h3>📝 Your Concern:</h3>
                            <p>%s</p>
                        </div>
                        
                        <div class="reply-box">
                            <h3>💬 Our Response:</h3>
                            <p>%s</p>
                        </div>
                        
                        <p>If you have any additional questions or concerns, please don't hesitate to contact us.</p>
                        
                        <p style="margin-top: 30px;">
                            <strong>Best regards,</strong><br>
                            Healthcare Management Team<br>
                            <em>Replied by: %s</em>
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p style="margin: 0;">© 2025 Healthcare System. All rights reserved.</p>
                        <p style="margin: 5px 0 0 0; font-size: 12px;">This is an automated email. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            concern.getPatientName(),
            concern.getConcernText(),
            concern.getReplyText(),
            concern.getRepliedBy()
        );
    }
}
