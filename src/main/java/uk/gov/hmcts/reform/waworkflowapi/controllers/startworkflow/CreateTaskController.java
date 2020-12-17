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
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnResponse;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.EvaluateDmnService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.SendMessageService;
import uk.gov.hmcts.reform.waworkflowapi.common.TaskToCreate;
import uk.gov.hmcts.reform.waworkflowapi.duedate.DueDateService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class CreateTaskController {

    public static final String CREATE_TASK_MESSAGE = "createTaskMessage";
    private final EvaluateDmnService evaluateDmnService;
    private final SendMessageService sendMessageService;
    private final DueDateService dueDateService;


    @Autowired
    public CreateTaskController(EvaluateDmnService evaluateDmnService,
                                SendMessageService sendMessageService,
                                DueDateService dueDateService) {
        this.evaluateDmnService = evaluateDmnService;
        this.sendMessageService = sendMessageService;
        this.dueDateService = dueDateService;
    }

    @PostMapping(path = "/workflow/decision-definition/key/{key}/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 200, message = "A DMN was found, evaluated and returned"),
    })
    public ResponseEntity<EvaluateDmnResponse> evaluateDmn(@RequestBody EvaluateDmnRequest evaluateDmnRequest,
                                                           @PathVariable(name = "key") String key) {
        List<Map<String, DmnValue<?>>> evaluateDmnResponse = evaluateDmnService.evaluateDmn(evaluateDmnRequest, key);
        return ResponseEntity.ok()
            .body(new EvaluateDmnResponse(evaluateDmnResponse));

    }

    @PostMapping(path = "/workflow/message", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new message was initiated"),
    })
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest sendMessageRequest) {

        if (CREATE_TASK_MESSAGE.equals(sendMessageRequest.getMessageName())) {
            sendMessageService.createMessage(updateDueDateInSendMessageRequest(sendMessageRequest));
        } else {
            sendMessageService.createMessage(sendMessageRequest);
        }

        return noContent().build();
    }

    private SendMessageRequest updateDueDateInSendMessageRequest(SendMessageRequest sendMessageRequest) {
        ZonedDateTime dueDateUkTime = getDueDate(sendMessageRequest);
        TaskToCreate taskToCreate = buildTaskToCreate(sendMessageRequest);
        ZonedDateTime updatedDueDate = dueDateService.calculateDueDate(dueDateUkTime, taskToCreate);
        Map<String, DmnValue<?>> updateProcessVariables = updateSendMessageRequestWithNewDueDate(
            sendMessageRequest,
            updatedDueDate
        );

        return new SendMessageRequest(
            sendMessageRequest.getMessageName(),
            updateProcessVariables,
            sendMessageRequest.getCorrelationKeys()
        );
    }

    private Map<String, DmnValue<?>> updateSendMessageRequestWithNewDueDate(SendMessageRequest sendMessageRequest,
                                                                            ZonedDateTime updatedDueDate) {
        Map<String, DmnValue<?>> updateProcessVariables = sendMessageRequest.getProcessVariables();
        updateProcessVariables.put(
            "dueDate",
            DmnValue.dmnStringValue(updatedDueDate.format(DateTimeFormatter.ISO_INSTANT))
        );
        updateProcessVariables.remove("workingDaysAllowed");
        return updateProcessVariables;
    }

    private TaskToCreate buildTaskToCreate(SendMessageRequest sendMessageRequest) {
        return new TaskToCreate(
            (String) sendMessageRequest.getProcessVariables().get("taskId").getValue(),
            (String) sendMessageRequest.getProcessVariables().get("group").getValue(),
            (Integer) sendMessageRequest.getProcessVariables().get("workingDaysAllowed").getValue(),
            (String) sendMessageRequest.getProcessVariables().get("name").getValue()
        );
    }

    private ZonedDateTime getDueDate(SendMessageRequest sendMessageRequest) {
        String dueDateAsString = (String) sendMessageRequest.getProcessVariables().get("dueDate").getValue();
        return (dueDateAsString == null) ? null : ZonedDateTime.parse(dueDateAsString);
    }

}
