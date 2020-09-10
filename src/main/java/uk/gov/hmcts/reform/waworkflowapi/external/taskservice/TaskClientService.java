package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.taskForId;

@Component
public class TaskClientService {
    private final CamundaClient camundaClient;

    @Autowired
    public TaskClientService(
        @Autowired CamundaClient camundaClient
    ) {
        this.camundaClient = camundaClient;
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Optional<TaskToCreate> getTask(Transition transition) {
        DmnRequest<GetTaskDmnRequest> requestParameters = new DmnRequest<>(new GetTaskDmnRequest(
            dmnStringValue(transition.getEventId()),
            dmnStringValue(transition.getPostState())
        ));

        List<GetTaskDmnResult> dmnResults = camundaClient.getTask(requestParameters);

        if (dmnResults.isEmpty()) {
            return Optional.empty();
        } else if (dmnResults.size() == 1) {
            Task task = taskForId(dmnResults.get(0).getTaskId().getValue());
            String group = dmnResults.get(0).getGroup().getValue();
            return Optional.of(new TaskToCreate(task, group));
        }
        throw new IllegalStateException("Should have exactly one task for transition");
    }

    public void createTask(String ccdId, TaskToCreate taskToCreate, String dueDate) {
        ProcessVariables processVariables = new ProcessVariables(
            ccdId,
            taskToCreate.getTask(),
            taskToCreate.getGroup(),
            dueDate
        );
        camundaClient.sendMessage(new SendMessageRequest("createTaskMessage", processVariables));
    }
}
