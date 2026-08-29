package com.mathias.coletti.controledegastos.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        ResponseEntity<String> ok = ResponseEntity.ok("API online!");
        return ok;
    }
}