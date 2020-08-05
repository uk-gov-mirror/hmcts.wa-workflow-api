package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class LivenessController {

    @GetMapping("/health/liveness")
    public ResponseEntity<String> welcome() {
        return ok("");
    }
}
