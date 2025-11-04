package com.intellihire.backend.controller;

import com.intellihire.backend.model.SaveJobRequest;
import com.intellihire.backend.model.SavedJob;
import com.intellihire.backend.repo.SavedJobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved")
@CrossOrigin
public class SavedJobController {

    private final SavedJobRepository repo;

    public SavedJobController(SavedJobRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<SavedJob> save(@RequestBody SaveJobRequest r) {
        var entity = SavedJob.builder()
                // .id(UUID.randomUUID())   // <<< REMOVE THIS
                .title(defaultStr(r.getTitle(), "Untitled role"))
                .company(emptyToNull(r.getCompany()))
                .location(emptyToNull(r.getLocation()))
                .remote(r.isRemote())
                .salaryMin(r.getSalaryMin())
                .salaryMax(r.getSalaryMax())
                .currency(emptyToNull(r.getCurrency()))
                .postedAt(r.getPostedAt())
                .description(emptyToNull(r.getDescription()))
                .applyUrl(emptyToNull(r.getApplyUrl()))
                .source(defaultStr(r.getSource(), "JSEARCH"))
                // createdAt will be set by @PrePersist
                .build();

        return ResponseEntity.ok(repo.save(entity)); // will INSERT with generated UUID
    }

    @GetMapping
    public List<SavedJob> list() {
        return repo.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (repo.existsById(id)) repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> deleteAll() {
        repo.deleteAllInBatch();     // fast bulk delete
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsv() {
        var rows = repo.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,company,location,remote,salary_min,salary_max,currency,posted_at,apply_url,source,created_at\n");
        for (var r : rows) {
            sb.append(esc(r.getId()))
                    .append(',').append(csv(r.getTitle()))
                    .append(',').append(csv(r.getCompany()))
                    .append(',').append(csv(r.getLocation()))
                    .append(',').append(r.isRemote())
                    .append(',').append(esc(r.getSalaryMin()))
                    .append(',').append(esc(r.getSalaryMax()))
                    .append(',').append(csv(r.getCurrency()))
                    .append(',').append(esc(r.getPostedAt()))
                    .append(',').append(csv(r.getApplyUrl()))
                    .append(',').append(csv(r.getSource()))
                    .append(',').append(esc(r.getCreatedAt()))
                    .append('\n');
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"saved-jobs.csv\"")
                .body(sb.toString());
    }

    private static String csv(String s) {
        if (s == null) return "";
        // quote if needed, escape inner quotes
        boolean needsQuote = s.contains(",") || s.contains("\"") || s.contains("\n");
        String t = s.replace("\"", "\"\"");
        return needsQuote ? "\"" + t + "\"" : t;
    }
    private static String esc(Object o) { return o == null ? "" : o.toString(); }

    private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private static String defaultStr(String s, String def) { return (s == null || s.isBlank()) ? def : s; }
}
