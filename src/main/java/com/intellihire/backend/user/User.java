// src/main/java/com/intellihire/backend/user/User.java
package com.intellihire.backend.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "app_user", indexes = {
        @Index(name = "ix_app_user_email", columnList = "email", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false) // DO NOT duplicate this field elsewhere
    private Instant createdAt;
}
