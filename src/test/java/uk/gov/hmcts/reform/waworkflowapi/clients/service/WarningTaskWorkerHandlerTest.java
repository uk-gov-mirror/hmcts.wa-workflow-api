package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.TaskManagementServiceApi;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.AddProcessVariableRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaTask;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.handler.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class WarningTaskWorkerHandlerTest {

    private static final String S2S_TOKEN = "some S2SToken";
    private static final String CASE_ID = "someCaseId";
    private static final String PROCESS_INSTANCE_ID = "some process instance Id";
    private static final String WARNING_VALUES = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private TaskManagementServiceApi taskManagementServiceApi;
    @Mock
    private CamundaClient camundaClient;
    @Mock
    private LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;
    private WarningTaskWorkerHandler warningTaskWorkerHandler;

    @BeforeEach
    void setUp() {

        warningTaskWorkerHandler = new WarningTaskWorkerHandler(
            taskManagementServiceApi,
            authTokenGenerator,
            launchDarklyFeatureFlagProvider,
            camundaClient
        );

        lenient().when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Nested
    @DisplayName("when release 2 cft task warning enabled")
    class FeatureFlagEnabled {

        @BeforeEach
        void setUp() {
            when(camundaClient.getProcessInstancesByVariables(
                S2S_TOKEN,
                "caseId_eq_" + CASE_ID,
                List.of("processStartTimer")
            ))
                .thenReturn(List.of(CamundaProcess.builder().id(PROCESS_INSTANCE_ID).build()));

        }

        @Test
        void should_complete_warning_external_task_Service() {
            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsToBeAdded = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsToBeAdded,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_not_complete_delay_warning_external_task_Service() {
            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsToBeAdded = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsToBeAdded,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                                           + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", "2020-01-01T10:58:12.184861").build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
            verify(camundaClient, never()).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_complete_warning_external_task_Service_with_out_updating_Cft_database_when_task_name_does_not_match() {
            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsToBeAdded = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsToBeAdded,
                "name", "Unknown"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                                           + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @NotNull
        private AddProcessVariableRequest getAddProcessVariableRequest(String expectedWarningValues) {
            Map<String, DmnValue<String>> modifications = new HashMap<>();
            try {
                WarningValues warningValues = new WarningValues(expectedWarningValues);

                String warning = warningValues.getValuesAsJson();
                modifications.put("warningList", DmnValue.dmnStringValue(warning));
            } catch (JsonProcessingException e) {
                // do nothing
            }
            return new AddProcessVariableRequest(modifications);
        }

        @Test
        void should_complete_warning_external_task_Service_with_duplicate_warnings() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_complete_warning_external_task_Service_with_different_warnings_text_but_same_code() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text2\"}]";

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text2\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";


            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequestWitDifferentCode());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_complete_warning_external_task_Service_with_same_warnings_text_but_different_code() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text1\"}]";

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text1\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";


            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequestWitDifferentText());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_complete_warning_external_task_Service_without_warnings() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);
            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_complete_warning_external_task_Service_without_warning_process_variable() {
            String expectedWarningValues = "[]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", expectedWarningValues)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "name", "SomeName"
            );
            when(externalTask.getAllVariables()).thenReturn(processVariables);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getEmptyWarningRequest());
            verify(camundaClient).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );
        }

        @Test
        void should_not_add_warning_to_non_delayed_tasks() {
            when(camundaClient.getProcessInstancesByVariables(
                S2S_TOKEN,
                "caseId_eq_" + CASE_ID,
                List.of("processStartTimer")
            ))
                .thenReturn(List.of());


            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";;
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient, never()).getProcessInstanceVariables(anyString(), anyString());
            verify(camundaClient, never()).updateProcessVariables(
                anyString(), anyString(), any(AddProcessVariableRequest.class));
        }

        @Test
        void should_handle_json_parsing_exception() {

            String processVariablesWarningValues = "[{\"warningCode\"\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                    .withProcessVariable("warningList", WARNING_VALUES)
                    .withProcessVariable("delayUntil", LocalDateTime.now().plusDays(1).toString()).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", "[]"
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getEmptyWarningRequest());

        }

        @Test
        void should_not_add_warning_to_non_delayed_tasks_when_process_list_is_null(CapturedOutput output) {
            when(camundaClient.getProcessInstancesByVariables(
                S2S_TOKEN,
                "caseId_eq_" + CASE_ID,
                List.of("processStartTimer")
            )).thenReturn(null);

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient, never()).getProcessInstanceVariables(anyString(), anyString());
            verify(camundaClient, never()).updateProcessVariables(
                anyString(), anyString(), any(AddProcessVariableRequest.class));

            String logMessage = "addWarningToDelayedProcesses can NOT continue to process due to camundaProcessList is null.";
            assertLogMessageContains(output, logMessage);
        }

        @Test
        void should_not_add_warning_to_non_delayed_tasks_when_process_is_null(CapturedOutput output) {
            List<CamundaProcess> camundaProcessList = new ArrayList<>();
            camundaProcessList.add(null);
            when(camundaClient.getProcessInstancesByVariables(
                S2S_TOKEN,
                "caseId_eq_" + CASE_ID,
                List.of("processStartTimer")
            ))
                .thenReturn(camundaProcessList);


            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient, never()).getProcessInstanceVariables(anyString(), anyString());
            verify(camundaClient, never()).updateProcessVariables(
                anyString(), anyString(), any(AddProcessVariableRequest.class));

            String logMessage = "addWarningToDelayedProcesses can NOT continue to process due to camundaProcess is null. "
                                + "caseId:someCaseId "
                                + "updatedWarningValues:[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            assertLogMessageContains(output, logMessage);
        }

        @Test
        void should_not_complete_delay_warning_external_task_Service_when_process_variables_are_null(CapturedOutput output) {
            Map<String, Object> processVariables = null;

            lenient().when(camundaClient.getProcessInstancesByVariables(
                S2S_TOKEN,
                "caseId_eq_" + CASE_ID,
                List.of("processStartTimer")
            )).thenReturn(List.of(CamundaProcess.builder().id(PROCESS_INSTANCE_ID).build()));

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                                           + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            verify(externalTaskService, never()).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
            verify(camundaClient, never()).updateProcessVariables(
                S2S_TOKEN, PROCESS_INSTANCE_ID,
                getAddProcessVariableRequest(expectedWarningValues)
            );

            String logMessage = "completeWarningTaskService can NOT continue to process due to externalTask is null.";
            assertLogMessageContains(output, logMessage);

        }

        @Test
        void should_not_update_warning_to_delayed_tasks(CapturedOutput output) {

            when(camundaClient.getProcessInstanceVariables(
                S2S_TOKEN,
                PROCESS_INSTANCE_ID
            )).thenReturn(null);

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "name", "SomeName"
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            List<CamundaTask> camundaTasks = getCamundaTaskList();

            when(warningTaskWorkerHandler.getTasks(CASE_ID)).thenReturn(camundaTasks);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);
            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getSingleWarningRequest());
            verify(camundaClient, times(1)).getProcessInstanceVariables(anyString(), anyString());
            verify(camundaClient, never()).updateProcessVariables(
                anyString(), anyString(), any(AddProcessVariableRequest.class));

            String logMessage = "updateDelayedProcessWarnings processVariables not found. ";
            assertLogMessageContains(output, logMessage);
        }

        private NotesRequest getExpectedWarningRequest() {
            List<NoteResource> noteResources = new ArrayList<>();
            noteResources.add(new NoteResource("Code2", "WARNING", "some-user", "Text2"));
            noteResources.add(new NoteResource("Code1", "WARNING", "some-user", "Text1"));
            return new NotesRequest(noteResources);
        }

        private NotesRequest getExpectedWarningRequestWitDifferentCode() {
            List<NoteResource> noteResources = new ArrayList<>();
            noteResources.add(new NoteResource("Code1", "WARNING", "some-user", "Text2"));
            noteResources.add(new NoteResource("Code1", "WARNING", "some-user", "Text1"));
            return new NotesRequest(noteResources);
        }

        private NotesRequest getExpectedWarningRequestWitDifferentText() {
            List<NoteResource> noteResources = new ArrayList<>();
            noteResources.add(new NoteResource("Code2", "WARNING", "some-user", "Text1"));
            noteResources.add(new NoteResource("Code1", "WARNING", "some-user", "Text1"));
            return new NotesRequest(noteResources);
        }


        private NotesRequest getEmptyWarningRequest() {
            List<NoteResource> noteResources = new ArrayList<>();
            return new NotesRequest(noteResources);
        }

        private NotesRequest getSingleWarningRequest() {
            List<NoteResource> noteResources = new ArrayList<>();
            noteResources.add(new NoteResource("Code1", "WARNING", "some-user", "Text1"));
            return new NotesRequest(noteResources);
        }

    }

    private List<CamundaTask> getCamundaTaskList() {
        return List.of(new CamundaTask(null,
            "SomeName",
            "SomeUser",
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            "SomeDescription",
            "SomeOwner",
            "SomeKey",
            "SomeProcessInstanceId"));
    }

    private void assertLogMessageContains(CapturedOutput output, String expectedMessage) {

        assertTrue(output.getOut().contains(expectedMessage));
    }
}
