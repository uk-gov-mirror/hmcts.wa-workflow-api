package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.time.ZonedDateTime;
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
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnIntegerValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskClientService.WA_TASK_INITIATION_DECISION_TABLE_NAME;

class TaskClientServiceTest {

    private CamundaClient camundaClient;
    private TaskClientService underTest;
    private String expectedTask;
    private Transition transition;
    private DmnRequest<GetTaskDmnRequest> dmnRequest;
    private static final String GROUP = "TCW";
    private static final String NAME = "taskName";
    private ServiceDetails serviceDetails;
    private AuthTokenGenerator authTokenGenerator;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        authTokenGenerator = mock(AuthTokenGenerator.class);
        underTest = new TaskClientService(camundaClient, authTokenGenerator);
        expectedTask = "processApplication";
        transition = new Transition("startState", "eventName", "endState");
        dmnRequest = new DmnRequest<>(new GetTaskDmnRequest(
            dmnStringValue(transition.getEventId()),
            dmnStringValue(transition.getPostState())
        ));
        serviceDetails = new ServiceDetails("jurisdiction", "casetype");

        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
    }

    @Test
    void getsATaskWithWorkingDaysBasedOnTransition() {
        int workingDaysAllowed = 5;
        List<GetTaskDmnResult> ts = singletonList(new GetTaskDmnResult(
            dmnStringValue(expectedTask),
            dmnStringValue(GROUP),
            dmnIntegerValue(workingDaysAllowed),
            dmnStringValue(NAME)
        ));
        when(camundaClient.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_INITIATION_DECISION_TABLE_NAME,
            serviceDetails.getJurisdiction(),
            serviceDetails.getCaseType(),
            dmnRequest
        )).thenReturn(ts);

        Optional<TaskToCreate> task = underTest.getTask(serviceDetails, transition);

        assertThat(task, is(Optional.of(new TaskToCreate(expectedTask, GROUP, workingDaysAllowed, NAME))));
    }

    @Test
    void getsATaskWithoutWorkingDaysBasedOnTransition() {
        List<GetTaskDmnResult> ts = singletonList(new GetTaskDmnResult(
            dmnStringValue(expectedTask),
            dmnStringValue(GROUP),
            null,
            dmnStringValue(NAME)
        ));

        when(camundaClient.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_INITIATION_DECISION_TABLE_NAME,
            serviceDetails.getJurisdiction(),
            serviceDetails.getCaseType(),
            dmnRequest
        )).thenReturn(ts);

        Optional<TaskToCreate> task = underTest.getTask(serviceDetails, transition);

        assertThat(task, is(Optional.of(new TaskToCreate(expectedTask, GROUP, NAME))));
    }

    @Test
    void noTasksForTransition() {
        List<GetTaskDmnResult> ts = emptyList();
        when(camundaClient.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_INITIATION_DECISION_TABLE_NAME,
            serviceDetails.getJurisdiction(),
            serviceDetails.getCaseType(),
            dmnRequest
        )).thenReturn(ts);

        Optional<TaskToCreate> task = underTest.getTask(serviceDetails, transition);

        assertThat(task, is(Optional.empty()));
    }

    @Test
    void getsMultipleTasksBasedOnTransitionWhichIsInvalid() {
        GetTaskDmnResult dmnResult = new GetTaskDmnResult(
            dmnStringValue(expectedTask),
            dmnStringValue("TCW"),
            dmnIntegerValue(5),
            dmnStringValue(NAME)
        );
        List<GetTaskDmnResult> ts = asList(dmnResult, dmnResult);
        when(camundaClient.evaluateDmnTable(
            BEARER_SERVICE_TOKEN,
            WA_TASK_INITIATION_DECISION_TABLE_NAME,
            serviceDetails.getJurisdiction(),
            serviceDetails.getCaseType(),
            dmnRequest
        )).thenReturn(ts);

        assertThrows(IllegalStateException.class, () -> {
            underTest.getTask(serviceDetails, transition);
        });
    }

    @Test
    void createsATask() {
        String caseId = "case_id";
        String group = "TCW";
        ZonedDateTime dueDate = ZonedDateTime.now().plusDays(2);

        underTest.createTask(serviceDetails, caseId, new TaskToCreate("processApplication", group, NAME), dueDate);

        Mockito.verify(camundaClient).sendMessage(
            BEARER_SERVICE_TOKEN,
            new SendMessageRequest(
                "createTaskMessage",
                new ProcessVariables(
                    serviceDetails.getJurisdiction(),
                    serviceDetails.getCaseType(),
                    caseId,
                    "processApplication",
                    group,
                    dueDate,
                    NAME
                )
            )
        );
    }
}
