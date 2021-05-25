package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @ApiOperation("Evaluates business rules given the specified decision definition key.")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "A decision definition was found, evaluated and it's output returned.",
            response = EvaluateDmnResponse.class
        ),
        @ApiResponse(
            code = 403,
            message = FORBIDDEN
        ),
        @ApiResponse(
            code = 401,
            message = UNAUTHORIZED
        ),
        @ApiResponse(
            code = 404,
            message = NOT_FOUND
        ),
        @ApiResponse(
            code = 415,
            message = UNSUPPORTED_MEDIA_TYPE
        ),
        @ApiResponse(
            code = 500,
            message = INTERNAL_SERVER_ERROR
        )
    })
    public ResponseEntity<EvaluateDmnResponse> evaluateDmn(@RequestBody EvaluateDmnRequest evaluateDmnRequest,
                                                           @PathVariable(name = "key") String key,
                                                           @PathVariable(name = "tenant-id") String tenantId) {
        List<Map<String, DmnValue<?>>> evaluateDmnResponse = evaluateDmnService.evaluateDmn(
            evaluateDmnRequest,
            key,
            tenantId
        );
        return ResponseEntity.ok()
            .body(new EvaluateDmnResponse(evaluateDmnResponse));

    }

    @PostMapping(path = "/workflow/message")
    @ApiOperation("Sends a message to the underlying business process engine.")
    @ApiResponses({
        @ApiResponse(
            code = 204,
            message = "The message was correlated to a business process",
            response = Object.class
        ),
        @ApiResponse(
            code = 400,
            message = BAD_REQUEST
        ),
        @ApiResponse(
            code = 403,
            message = FORBIDDEN
        ),
        @ApiResponse(
            code = 401,
            message = UNAUTHORIZED
        ),
        @ApiResponse(
            code = 415,
            message = UNSUPPORTED_MEDIA_TYPE
        ),
        @ApiResponse(
            code = 500,
            message = INTERNAL_SERVER_ERROR
        )
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {

        sendMessageService.createMessage(sendMessageRequest);
        return noContent().build();
    }

}
