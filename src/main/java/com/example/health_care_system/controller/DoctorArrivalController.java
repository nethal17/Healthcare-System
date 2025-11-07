package com.example.health_care_system.controller;

import com.example.health_care_system.service.NotificationDispatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
public class DoctorArrivalController {

    private final NotificationDispatcherService dispatcherService;
    private static final Logger logger = LoggerFactory.getLogger(DoctorArrivalController.class);

    public DoctorArrivalController(NotificationDispatcherService dispatcherService) {
        this.dispatcherService = dispatcherService;
    }

    @PatchMapping("/{id}/arrived")
    public ResponseEntity<?> doctorArrived(@PathVariable("id") String id,
                                           @RequestBody(required = false) java.util.Map<String, Object> payload) {
        logger.info("Doctor arrived endpoint invoked for id={} payload={}", id, payload);
        dispatcherService.notifyDoctorArrived(id);
        return ResponseEntity.accepted().body("Notifications queued");
    }
}
