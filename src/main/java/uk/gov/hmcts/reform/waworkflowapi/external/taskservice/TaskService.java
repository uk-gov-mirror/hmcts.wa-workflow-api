package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.util.Optional;

@Component
public class TaskService {
    private final TaskClientService taskClientService;

    @Autowired
    public TaskService(TaskClientService taskClientService) {
        this.taskClientService = taskClientService;
    }

    public boolean createTask(Transition transition, String caseId) {
        Optional<Task> taskOptional = taskClientService.getTask(transition);

        return taskOptional.map(task -> {
            taskClientService.createTask(caseId, task);

            return true;
        }).orElse(false);
    }
}
