package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskService;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class CreateTaskController {

    private final TaskService taskService;

    @PostMapping(path = "/tasks", consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Starts the workflow to create a task")
    @ApiImplicitParam(name = "ServiceAuthorization", value = "Bearer xxxx", paramType = "header")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new task has been created for the transition"),
        @ApiResponse(code = 204, message = "No new task was created for the transition")
    })
    public ResponseEntity<Object> createTask(@RequestBody CreateTaskRequest createTaskRequest) {
        if (taskService.createTask(
            createTaskRequest.getServiceDetails(),
            createTaskRequest.getTransition(),
            createTaskRequest.getCaseId(),
            createTaskRequest.getDueDate()
        )) {
            return created(null).build();
        } else {
            return noContent().build();
        }
    }

    @Autowired
    public CreateTaskController(TaskService taskService) {
        this.taskService = taskService;
    }
}
