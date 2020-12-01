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
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskService;

import java.net.URI;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class CreateTaskController {

    private final TaskService taskService;

    @Autowired
    public CreateTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping(path = "/workflow/message", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new task has been created for the transition"),
        @ApiResponse(code = 204, message = "No new task was created for the transition")
    })
    public ResponseEntity<Object> createMessage(@RequestBody CreateTaskRequest createTaskRequest) {
            if (taskService.createTask(
                createTaskRequest.getServiceDetails(),
                createTaskRequest.getTransition(),
                createTaskRequest.getCaseId(),
                createTaskRequest.getDueDate(),
                null
            )) {
                return created(URI.create("/tasks")).build();
            } else {
                return noContent().build();
            }
        }


    @PostMapping(path = "/workflow/decision-definition/{id}/evaluate", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Creates a message form camunda")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new task has been created for the transition"),
        @ApiResponse(code = 204, message = "No new task was created for the transition")
    })
    public ResponseEntity<Object> evaluateDmn(@RequestBody CreateTaskRequest createTaskRequest, @PathVariable String id) {
                if (taskService.createTask(
                    createTaskRequest.getServiceDetails(),
                    createTaskRequest.getTransition(),
                    createTaskRequest.getCaseId(),
                    createTaskRequest.getDueDate(),
                    id
                )) {
                    return created(URI.create("/tasks")).build();
                } else {
                    return noContent().build();
                }
            }

}
