package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskManagerService;

import java.util.Optional;

import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class StartWorkflowController {

    private final Logger log = LoggerFactory.getLogger(StartWorkflowController.class);

    private final TaskManagerService taskManagerService;

    @Autowired
    public StartWorkflowController(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @PostMapping(path = "/tasks", consumes = { MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Starts the workflow to create a task")
    @ApiResponses({
        @ApiResponse(code = 201, message = "A new task has been created for the transition"),
        @ApiResponse(code = 204, message = "No new task was created for the transition")
    })
    public ResponseEntity createTaskWorkflow(@RequestBody CreateTaskRequest createTaskRequest) {
        Optional<Task> task = taskManagerService.getTask(createTaskRequest.getTransition());
        log.info("Got task [" + task + "]");

        return task.map(taskEnum -> created(null).build())
            .orElse(noContent().build());
    }
}
