package com.intellihire.backend.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class SaveJobRequest {
    @NotBlank private String title;
    private String company;
    private String location;
    private boolean remote;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;

    private Instant postedAt;     // ISO string from client is fine
    private String description;
    private String applyUrl;
    private String source;        // e.g., "JSEARCH@jsearch.p.rapidapi.com"
}
