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

    public boolean createTask(Transition transition, String caseId, String dueDate) {
        Optional<TaskToCreate> taskOptional = taskClientService.getTask(transition);

        return taskOptional.map(taskToCreate -> {
            taskClientService.createTask(caseId, taskToCreate, dueDate);

            return true;
        }).orElse(false);
    }
}
