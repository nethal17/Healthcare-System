package com.example.health_care_system.controller;

import com.example.health_care_system.model.TimeSlotReservation;
import com.example.health_care_system.repository.TimeSlotReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Debug controller to inspect MongoDB state
 */
@RestController
@RequestMapping("/debug/reservations")
public class ReservationDebugController {
    
    @Autowired
    private TimeSlotReservationRepository reservationRepository;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Get ALL reservations (including expired/cancelled)
     */
    @GetMapping("/all")
    public Map<String, Object> getAllReservations() {
        List<TimeSlotReservation> all = reservationRepository.findAll();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", all.size());
        response.put("reservations", all);
        
        // Count by status
        long active = all.stream().filter(r -> r.getStatus() == TimeSlotReservation.ReservationStatus.ACTIVE).count();
        long confirmed = all.stream().filter(r -> r.getStatus() == TimeSlotReservation.ReservationStatus.CONFIRMED).count();
        long cancelled = all.stream().filter(r -> r.getStatus() == TimeSlotReservation.ReservationStatus.CANCELLED).count();
        long expired = all.stream().filter(r -> r.getStatus() == TimeSlotReservation.ReservationStatus.EXPIRED).count();
        
        response.put("activeCount", active);
        response.put("confirmedCount", confirmed);
        response.put("cancelledCount", cancelled);
        response.put("expiredCount", expired);
        
        return response;
    }
    
    /**
     * Get collection info including indexes
     */
    @GetMapping("/collection-info")
    public Map<String, Object> getCollectionInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if collection exists
            boolean exists = mongoTemplate.collectionExists(TimeSlotReservation.class);
            response.put("collectionExists", exists);
            
            if (exists) {
                // Get collection stats
                var collection = mongoTemplate.getCollection("time_slot_reservations");
                response.put("documentCount", collection.countDocuments());
                
                // List indexes
                var indexes = collection.listIndexes();
                List<org.bson.Document> indexList = new java.util.ArrayList<>();
                indexes.into(indexList);
                response.put("indexes", indexList);
            }
            
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
}
