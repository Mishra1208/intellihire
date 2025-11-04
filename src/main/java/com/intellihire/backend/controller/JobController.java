package com.intellihire.backend.controller;

import com.intellihire.backend.model.Job;
import com.intellihire.backend.model.SearchRequest;
import com.intellihire.backend.service.JSearchClient;
import com.intellihire.backend.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin // in dev: allow your Vite frontend; restrict in prod
public class JobController {

    private final JSearchClient jsearch;
    private final RankingService ranking;

    public JobController(JSearchClient jsearch, RankingService ranking) {
        this.jsearch = jsearch;
        this.ranking = ranking;
    }

    /**
     * Search jobs via JSearch and (optionally) rank by user skills.
     *
     * @param q         required query, e.g., "Java Developer"
     * @param location  optional location, e.g., "Montreal"
     * @param remote    default true (remote-friendly roles)
     * @param page      1-based page (default 1)
     * @param limit     max results to return (default 20, capped to 50)
     * @param skills    comma-separated skills for ranking (e.g., "java,spring,aws")
     * @param rank      whether to apply ranking (default true). If false, returns raw API order.
     */
    @GetMapping
    public ResponseEntity<List<Job>> search(
            @RequestParam String q,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "true") boolean remote,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String skills,
            @RequestParam(defaultValue = "true") boolean rank
    ) {
        int safePage  = Math.max(1, page);
        int safeLimit = Math.min(50, Math.max(1, limit)); // protect upstream and UI

        var req = new SearchRequest(q, location, remote, safePage, safeLimit);
        var jobs = jsearch.search(req);

        if (!rank) {
            return ResponseEntity.ok(jobs.stream().limit(safeLimit).collect(Collectors.toList()));
        }

        // skills from querystring, or V1__init.sql decent default
        Set<String> skillSet = parseSkillsOrDefault(skills,
                Set.of("java", "spring", "spring boot", "postgres", "aws"));

        var ranked = ranking.topK(jobs, skillSet, safeLimit);
        return ResponseEntity.ok(ranked);
    }

    /* -------- helpers -------- */

    private static Set<String> parseSkillsOrDefault(String skillsCsv, Set<String> fallback) {
        if (!StringUtils.hasText(skillsCsv)) return fallback;
        var out = Arrays.stream(skillsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return out.isEmpty() ? fallback : out;
    }
}
