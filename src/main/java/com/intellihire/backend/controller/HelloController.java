package com.intellihire.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/health")
    public String healthCheck() {
        return "IntelliHire backend is running.";
    }
}
