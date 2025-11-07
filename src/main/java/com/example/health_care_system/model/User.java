package com.example.health_care_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;

import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public class User {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private UserRole role;

    private String gender;

    private String contactNumber;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
