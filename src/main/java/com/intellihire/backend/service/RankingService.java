package com.intellihire.backend.service;

import com.intellihire.backend.model.Job;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final Map<String,Integer> weight = Map.of(
            "java",3, "spring",3, "spring boot",4, "postgres",2, "mysql",2, "aws",2, "docker",1);

    public int keywordScore(Job j, Set<String> skills) {
        String text = (safe(j.getTitle()) + " " + safe(j.getDescription())).toLowerCase();
        int s = 0;
        for (String k : skills) {
            String t = k.toLowerCase();
            if (text.contains(t)) s += weight.getOrDefault(t, 1);
        }
        return s;
    }

    public double recency(Job j) {
        if (j.getPostedAt() == null) return 0.0;
        long days = ChronoUnit.DAYS.between(j.getPostedAt(), Instant.now());
        return Math.exp(-days / 14.0);
    }

    public List<Job> topK(List<Job> jobs, Set<String> skills, int k) {
        record RJ(Job j,double s){}
        PriorityQueue<RJ> pq = new PriorityQueue<>(Comparator.comparingDouble(r -> r.s));
        for (Job j : jobs) {
            double score = 3.0 * keywordScore(j, skills) + 10.0 * recency(j);
            pq.offer(new RJ(j, score));
            if (pq.size() > k) pq.poll();
        }
        return pq.stream()
                .sorted(Comparator.comparingDouble((RJ r)->r.s).reversed())
                .map(r -> r.j)
                .collect(Collectors.toList());
    }

    private String safe(String x){ return x==null?"":x; }
}
