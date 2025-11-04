package com.intellihire.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellihire.backend.model.Job;
import com.intellihire.backend.model.SearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JSearchClient {

    private final WebClient http;
    private final String host;

    public JSearchClient(
            @Value("${app.rapidapi.key}") String key,
            @Value("${app.rapidapi.jsearchHost}") String host
    ) {
        this.host = host;

        // Small timeout + larger buffer for verbose postings
        HttpClient reactor = HttpClient.create().responseTimeout(Duration.ofSeconds(8));

        this.http = WebClient.builder()
                .baseUrl("https://" + host)
                .defaultHeader("X-RapidAPI-Key", key)
                .defaultHeader("X-RapidAPI-Host", host)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(reactor))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024)) // 4MB
                        .build())
                .build();
    }

    public List<Job> search(SearchRequest r) {
        JsonNode root = http.get()
                .uri(uri -> uri.path("/search")
                        .queryParam("query", (r.qSafe() + " " + r.locSafe()).trim())
                        .queryParam("page", r.pageSafe())
                        .queryParam("num_pages", 1)
                        .queryParam("remote_jobs_only", r.remoteSafe())
                        .build())
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return resp.bodyToMono(JsonNode.class);
                    }
                    // Bubble up V1__init.sql readable error body so controllers can show nice JSON
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                String msg = "JSearch " + resp.statusCode().value() + " -> " + body;
                                log.warn(msg);
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, msg));
                            });
                })
                .block();

        List<Job> out = new ArrayList<>();
        if (root == null) return out;

        JsonNode data = root.get("data");
        if (data == null || !data.isArray()) return out;

        for (JsonNode d : data) {
            out.add(Job.builder()
                    .source("JSEARCH@" + host)
                    .title(textOrNull(d, "job_title"))
                    .company(textOrNull(d, "employer_name"))
                    .location(buildLocation(d))
                    .remote(d.path("job_is_remote").asBoolean(false))
                    .salaryMin(decimalOrNull(d, "job_min_salary"))
                    .salaryMax(decimalOrNull(d, "job_max_salary"))
                    .currency(textOrNull(d, "job_salary_currency"))
                    .postedAt(parseTs(textOrNull(d, "job_posted_at_datetime_utc")))
                    .description(textOrNull(d, "job_description", ""))
                    .applyUrl(resolveApplyUrl(d))
                    .build());
        }
        return out;
    }

    /* -------------------- helpers -------------------- */

    private static String textOrNull(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static String textOrNull(JsonNode n, String field, String defaultVal) {
        JsonNode v = n.get(field);
        return (v == null || v.isNull()) ? defaultVal : v.asText(defaultVal);
    }

    private static BigDecimal decimalOrNull(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return (v == null || v.isNull() || !v.isNumber()) ? null : v.decimalValue();
    }

    private static String buildLocation(JsonNode d) {
        String city = textOrNull(d, "job_city");
        String state = textOrNull(d, "job_state");
        String country = textOrNull(d, "job_country");
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isBlank()) sb.append(city);
        if (state != null && !state.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(state);
        }
        if (country != null && !country.isBlank()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(country);
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static String resolveApplyUrl(JsonNode d) {
        // Prefer direct link; fall back to first "apply_options[].apply_link" if needed
        String direct = textOrNull(d, "job_apply_link");
        if (direct != null && !direct.isBlank()) return direct;

        JsonNode opts = d.get("apply_options");
        if (opts != null && opts.isArray() && !opts.isEmpty()) {
            for (JsonNode opt : opts) {
                String link = textOrNull(opt, "apply_link");
                if (link != null && !link.isBlank()) return link;
            }
        }
        return null;
    }

    private static Instant parseTs(String s) {
        try { return (s == null || s.isBlank()) ? null : Instant.parse(s); }
        catch (Exception e) { return null; }
    }
}
