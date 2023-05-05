package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.UnAuthorizedException;

import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@RequestMapping(
    consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE
)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class WorkflowApiTestingController {

    public static final String UNAUTHORIZED = "Unauthorized";
    private static final String NOT_FOUND = "Not Found";
    private final IdempotencyTaskService idempotencyTaskService;

    @Value("${environment}")
    private String environment;

    @Autowired
    public WorkflowApiTestingController(IdempotencyTaskService idempotencyTaskService) {
        this.idempotencyTaskService = idempotencyTaskService;
    }

    @GetMapping(path = "/workflow/idempotency/{idempotency_key}/{jurisdiction}")
    @Operation(summary = "Handles the idempotency check request")
    @ApiResponse(
        responseCode = "200",
        description = "Idempotency check",
        content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = NOT_FOUND
    )
    @ApiResponse(
        responseCode = "401",
        description = UNAUTHORIZED
    )
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public ResponseEntity<IdempotencyKeys> checkIdempotencyKey(@PathVariable("idempotency_key") final String idempotencyKey,
                                                               @PathVariable("jurisdiction") final String jurisdiction) {
        if (!isNonProdEnvironment()) {
            throw new UnAuthorizedException("Request Unauthorized for this environment");
        }

        Optional<IdempotencyKeys> idempotencyKeysOptional =
            idempotencyTaskService.findByIdempotencyKeyAndTenantId(idempotencyKey, jurisdiction);

        if (idempotencyKeysOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                String.format("Resource not found idempotencyKey:%s jurisdiction:%s", idempotencyKey, jurisdiction));
        }

        return ResponseEntity
            .ok()
            .body(idempotencyKeysOptional.get());

    }

    private boolean isNonProdEnvironment() {
        log.info("Processing message in '{}' environment ", environment);
        return !"prod".equalsIgnoreCase(environment);
    }
}
