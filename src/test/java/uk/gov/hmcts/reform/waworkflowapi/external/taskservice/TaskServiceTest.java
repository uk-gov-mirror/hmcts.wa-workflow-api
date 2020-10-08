package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;
import uk.gov.hmcts.reform.waworkflowapi.duedate.DueDateService;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.PROCESS_APPLICATION;

class TaskServiceTest {

    private String someCcdId;
    private TaskClientService taskClientService;
    private TaskService underTest;
    private Transition someTransition;
    private Task taskBeingCreated;
    private ZonedDateTime dueDate;
    private DueDateService dueDateService;
    private ServiceDetails serviceDetails;

    @BeforeEach
    void setUp() {
        someCcdId = "ccdId";
        someTransition = new Transition("preState", "eventId", "postState");
        taskBeingCreated = PROCESS_APPLICATION;
        dueDate = ZonedDateTime.now().plusDays(2);

        taskClientService = mock(TaskClientService.class);
        dueDateService = mock(DueDateService.class);
        underTest = new TaskService(taskClientService, dueDateService);
        serviceDetails = new ServiceDetails("some_jurisdiction", "some_case_type");
    }

    @Test
    void createsATask() {
        TaskToCreate taskToCreate = new TaskToCreate(this.taskBeingCreated, "TCW", "task name");
        when(taskClientService.getTask(serviceDetails, someTransition)).thenReturn(Optional.of(taskToCreate));
        ZonedDateTime calculatedDueDate = ZonedDateTime.now();
        when(dueDateService.calculateDueDate(this.dueDate, taskToCreate)).thenReturn(calculatedDueDate);

        boolean createdTask = underTest.createTask(serviceDetails, someTransition, someCcdId, this.dueDate);

        assertThat("Should have created a task", createdTask, CoreMatchers.is(true));
        verify(taskClientService).createTask(serviceDetails, someCcdId, taskToCreate, calculatedDueDate);
    }

    @Test
    void doesNotCreateATask() {
        when(taskClientService.getTask(serviceDetails, someTransition)).thenReturn(Optional.empty());

        boolean createdTask = underTest.createTask(serviceDetails, someTransition, someCcdId, dueDate);

        assertThat("Should not have created a task", createdTask, CoreMatchers.is(false));
        verify(taskClientService, never()).createTask(
            any(ServiceDetails.class),
            any(String.class), any(TaskToCreate.class), any(ZonedDateTime.class));
    }

}
