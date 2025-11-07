package com.example.health_care_system.repository;

import com.example.health_care_system.model.NotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, String> {
}
