package com.intellihire.backend.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class Job {
    private String source;
    private String title;
    private String company;
    private String location;
    private Boolean remote;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private Instant postedAt;
    private String description;
    private String applyUrl;
}
