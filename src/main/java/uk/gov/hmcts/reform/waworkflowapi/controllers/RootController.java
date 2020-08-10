package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
public class RootController {

    private final Logger log = LoggerFactory.getLogger(RootController.class);
    private final String testProperty;

    public RootController(@Value("${testProperty}") String testProperty) {
        this.testProperty = testProperty;
    }

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        log.info("Root controller");
        return ok("Welcome to wa-workflow-api [" + testProperty + "] 4");
    }
}
