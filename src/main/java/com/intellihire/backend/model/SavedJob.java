package com.intellihire.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saved_job")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   // <<< let Hibernate create the UUID
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String company;
    private String location;
    private boolean remote;

    @Column(name = "salary_min")
    private BigDecimal salaryMin;

    @Column(name = "salary_max")
    private BigDecimal salaryMax;

    @Column(length = 8)
    private String currency;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "apply_url")
    private String applyUrl;

    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
