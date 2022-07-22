package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnResponse;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.EvaluateDmnService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.SendMessageService;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.noContent;

@Slf4j
@RestController
@RequestMapping(
    consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE
)
public class CreateTaskController {

    public static final String UNAUTHORIZED = "Unauthorized";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String FORBIDDEN = "Forbidden";
    private static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String NOT_FOUND = "Not Found";

    private final EvaluateDmnService evaluateDmnService;
    private final SendMessageService sendMessageService;


    @Autowired
    public CreateTaskController(EvaluateDmnService evaluateDmnService,
                                SendMessageService sendMessageService) {
        this.evaluateDmnService = evaluateDmnService;
        this.sendMessageService = sendMessageService;
    }

    @PostMapping(
        path = "/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate"
    )
    @Operation(summary = "Evaluates business rules given the specified decision definition key.")
    @ApiResponse(
        responseCode = "200",
        description = "A decision definition was found, evaluated and it's output returned.",
        content = @Content(schema = @Schema(implementation = EvaluateDmnResponse.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = FORBIDDEN
    )
    @ApiResponse(
        responseCode = "401",
        description = UNAUTHORIZED
    )
    @ApiResponse(
        responseCode = "404",
        description = NOT_FOUND
    )
    @ApiResponse(
        responseCode = "415",
        description = UNSUPPORTED_MEDIA_TYPE
    )
    @ApiResponse(
        responseCode = "500",
        description = INTERNAL_SERVER_ERROR
    )
    public ResponseEntity<EvaluateDmnResponse> evaluateDmn(@RequestBody EvaluateDmnRequest evaluateDmnRequest,
                                                           @PathVariable(name = "key") String key,
                                                           @PathVariable(name = "tenant-id") String tenantId) {
        log.info("evaluateDmn  evaluateDmnRequest={}, key={}, tenantId={}", evaluateDmnRequest,key,tenantId);
        List<Map<String, DmnValue<?>>> evaluateDmnResponse = evaluateDmnService.evaluateDmn(
            evaluateDmnRequest,
            key,
            tenantId
        );
        log.info("evaluateDmnResponse= {}", evaluateDmnResponse);
        return ResponseEntity.ok()
            .body(new EvaluateDmnResponse(evaluateDmnResponse));

    }

    @PostMapping(path = "/workflow/message")
    @Operation(summary = "Sends a message to the underlying business process engine.")
    @ApiResponse(
        responseCode = "204",
        description = "The message was correlated to a business process",
        content = @Content(schema = @Schema(implementation = Object.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = BAD_REQUEST
    )
    @ApiResponse(
        responseCode = "403",
        description = FORBIDDEN
    )
    @ApiResponse(
        responseCode = "401",
        description = UNAUTHORIZED
    )
    @ApiResponse(
        responseCode = "404",
        description = NOT_FOUND
    )
    @ApiResponse(
        responseCode = "415",
        description = UNSUPPORTED_MEDIA_TYPE
    )
    @ApiResponse(
        responseCode = "500",
        description = INTERNAL_SERVER_ERROR
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {

        sendMessageService.createMessage(sendMessageRequest);
        return noContent().build();
    }

}
