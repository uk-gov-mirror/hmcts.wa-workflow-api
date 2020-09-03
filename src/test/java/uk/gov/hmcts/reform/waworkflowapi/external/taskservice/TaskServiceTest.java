package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

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

    @BeforeEach
    void setUp() {
        someCcdId = "ccdId";
        someTransition = new Transition("preState", "eventId", "postState");
        taskBeingCreated = PROCESS_APPLICATION;

        taskClientService = mock(TaskClientService.class);
        underTest = new TaskService(taskClientService);
    }

    @Test
    void createsATask() {
        taskClientService = mock(TaskClientService.class);
        underTest = new TaskService(taskClientService);
        TaskToCreate taskToCreate = new TaskToCreate(this.taskBeingCreated, "TCW");
        when(taskClientService.getTask(someTransition)).thenReturn(Optional.of(taskToCreate));

        boolean createdTask = underTest.createTask(someTransition, someCcdId);

        assertThat("Should have created a task", createdTask, CoreMatchers.is(true));
        verify(taskClientService).createTask(someCcdId, taskToCreate);
    }

    @Test
    void doesNotCreateATask() {
        when(taskClientService.getTask(someTransition)).thenReturn(Optional.empty());

        boolean createdTask = underTest.createTask(someTransition, someCcdId);

        assertThat("Should not have created a task", createdTask, CoreMatchers.is(false));
        verify(taskClientService, never()).createTask(any(String.class), any(TaskToCreate.class));
    }

}
