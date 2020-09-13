package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.waworkflowapi.camuda.rest.api.wrapper.CamundaTaskService;

@RestController
@SuppressWarnings("PMD.UnnecessaryLocalBeforeReturn")
public class GetTaskController {

    private final CamundaTaskService camundaTaskService;

    @Autowired
    public GetTaskController(CamundaTaskService camundaTaskService) {
        this.camundaTaskService = camundaTaskService;
    }

    @GetMapping(path = "/task/{task-id}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public String getTask(@PathVariable("task-id") String id)  {
        return camundaTaskService.getTask(id);
    }

}
