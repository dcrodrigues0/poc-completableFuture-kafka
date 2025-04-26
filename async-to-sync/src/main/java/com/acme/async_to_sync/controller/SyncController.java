package com.acme.async_to_sync.controller;

import com.acme.async_to_sync.service.SyncKafkaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final SyncKafkaService syncKafkaService;

    public SyncController(SyncKafkaService syncKafkaService) {
        this.syncKafkaService = syncKafkaService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody String request) {
        try {
            String response = syncKafkaService.sendAndWait(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("Request timeout or error");
        }
    }

}
