package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;
import uk.gov.hmcts.reform.waworkflowapi.duedate.DueDateService;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
public class TaskService {
    private final TaskClientService taskClientService;
    private final DueDateService dueDateService;

    @Autowired
    public TaskService(TaskClientService taskClientService, DueDateService dueDateService) {
        this.taskClientService = taskClientService;
        this.dueDateService = dueDateService;
    }

    public boolean createTask(
        ServiceDetails serviceDetails,
        Transition transition,
        String caseId,
        ZonedDateTime dueDate,
        String id
    ) {
        if (id == null) {
            Optional<TaskToCreate> taskOptional = taskClientService.getTask(serviceDetails, transition);

            return taskOptional.map(taskToCreate -> {
                ZonedDateTime calculatedDueDate = dueDateService.calculateDueDate(dueDate, taskToCreate);
                taskClientService.createTask(serviceDetails, caseId, taskToCreate, calculatedDueDate);
                return true;
            }).orElse(false);
        } else {
            taskClientService.getTask(serviceDetails, transition);
            return true;
        }
    }
}
