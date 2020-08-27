package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.PROCESS_APPLICATION;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.taskForId;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
public class TaskManagerServiceTest {

    private CamundaClient camundaClient;
    private TaskManagerService underTest;
    private String expectedTask;
    private Transition transition;
    private DmnRequest<GetTaskDmnRequest> dmnRequest;

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        underTest = new TaskManagerService(camundaClient);
        expectedTask = PROCESS_APPLICATION.getId();
        transition = new Transition("startState", "eventName", "endState");
        dmnRequest = new DmnRequest<>(new GetTaskDmnRequest(
            dmnStringValue(transition.getEventId()),
            dmnStringValue(transition.getPostState())
        ));
    }

    @Test
    public void getsATaskBasedOnTransition() {
        List<GetTaskDmnResult> ts = singletonList(new GetTaskDmnResult(dmnStringValue(expectedTask)));
        when(camundaClient.getTask(dmnRequest)).thenReturn(ts);

        Optional<Task> task = underTest.getTask(transition);

        assertThat(task, is(Optional.of(taskForId(expectedTask))));
    }

    @Test
    public void noTasksForTransition() {
        List<GetTaskDmnResult> ts = emptyList();
        when(camundaClient.getTask(dmnRequest)).thenReturn(ts);

        Optional<Task> task = underTest.getTask(transition);

        assertThat(task, is(Optional.empty()));
    }

    @Test
    public void getsMultipleTasksBasedOnTransitionWhichIsInvalid() {
        GetTaskDmnResult dmnResult = new GetTaskDmnResult(dmnStringValue(expectedTask));
        List<GetTaskDmnResult> ts = asList(dmnResult, dmnResult);
        when(camundaClient.getTask(dmnRequest)).thenReturn(ts);

        assertThrows(IllegalStateException.class, () -> {
            underTest.getTask(transition);
        });
    }
}
