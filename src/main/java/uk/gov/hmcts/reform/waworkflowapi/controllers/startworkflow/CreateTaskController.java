package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnResponse;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnService;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageService;

import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class CreateTaskController {

    private final EvaluateDmnService evaluateDmnService;
    private final SendMessageService sendMessageService;

    @Autowired
    public CreateTaskController(EvaluateDmnService evaluateDmnService,
                                SendMessageService sendMessageService
    ) {
        this.evaluateDmnService = evaluateDmnService;
        this.sendMessageService = sendMessageService;
    }

    @PostMapping(path = "/workflow/decision-definition/{id}/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 200, message = "A DMN was found, evaluated and returned"),
    })
    public ResponseEntity<EvaluateDmnResponse> evaluateDmn(@RequestBody EvaluateDmnRequest evaluateDmnRequest, @PathVariable String id) {
        List<Map<String,Object>> evaluateDmnResponse = evaluateDmnService.evaluateDmn(evaluateDmnRequest, id);
        return ResponseEntity.ok()
            .body(new EvaluateDmnResponse(evaluateDmnResponse));

    }


    @PostMapping(path = "/workflow/message", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new message was initiated"),
    })
    public ResponseEntity<Object> sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {
        sendMessageService.createMessage(sendMessageRequest);
        return noContent().build();
    }

}
