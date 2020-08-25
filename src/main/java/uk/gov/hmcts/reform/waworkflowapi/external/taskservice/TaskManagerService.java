package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.taskForId;

@Component
public class TaskManagerService {
    private final CamundaClient camundaClient;

    @Autowired
    public TaskManagerService(
        @Autowired CamundaClient camundaClient
    ) {
        this.camundaClient = camundaClient;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<Task> getTask(Transition transition) {
        DmnRequest<GetTaskDmnRequest> requestParameters = new DmnRequest<>(new GetTaskDmnRequest(
            new DmnValue(transition.getEventId(), "String"),
            new DmnValue(transition.getPostState(), "String")
        ));

        List<GetTaskDmnResult> dmnResults = camundaClient.getTask(requestParameters);

        if (dmnResults.isEmpty()) {
            return Optional.empty();
        } else if (dmnResults.size() == 1) {
            return Optional.of(taskForId(dmnResults.get(0).getTaskId().getValue()));
        }
        throw new IllegalStateException("Should have exactly one task for transition");
    }
}
