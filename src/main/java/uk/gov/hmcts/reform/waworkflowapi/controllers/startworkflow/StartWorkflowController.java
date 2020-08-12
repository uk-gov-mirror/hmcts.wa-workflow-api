package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
public class StartWorkflowController {

    private final Logger log = LoggerFactory.getLogger(StartWorkflowController.class);

    @PostMapping(path = "/tasks", consumes = { MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation("Starts the workflow to create a task")
    @ApiResponses({
        @ApiResponse(code = 204, message = "The workflow has been started")
    })
    public ResponseEntity createTaskWorkflow(@RequestBody CreateTaskRequest createTaskRequest) {
        log.info("Got start task request");
        return noContent().build();
    }
}
