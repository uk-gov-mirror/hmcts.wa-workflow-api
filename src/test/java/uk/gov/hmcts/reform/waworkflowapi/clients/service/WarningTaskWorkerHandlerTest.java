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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.TaskManagementServiceApi;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.AddProcessVariableRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.handler.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag.RELEASE_2_CFT_TASK_WARNING;


@ExtendWith(MockitoExtension.class)
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
            when(launchDarklyFeatureFlagProvider.getBooleanValue(RELEASE_2_CFT_TASK_WARNING)).thenReturn(true);
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
                "warningsToAdd", warningsToBeAdded
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

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
                "warningsToAdd", warningsFromHandler
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

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
        void should_complete_warning_external_task_Service_with_different_warnings_text_but_same_code() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text2\"}]";

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text2\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";


            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

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
        void should_complete_warning_external_task_Service_with_same_warnings_text_but_different_code() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text1\"}]";

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text1\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";


            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

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
        void should_complete_warning_external_task_Service_without_warnings() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true,
                "warningList", processVariablesWarningValues
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);

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
        void should_complete_warning_external_task_Service_without_warning_process_variable() {
            String expectedWarningValues = "[]";

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", expectedWarningValues).build());

            Map<String, Object> processVariables = Map.of(
                "caseId", CASE_ID,
                "hasWarnings", true
            );
            when(externalTask.getAllVariables()).thenReturn(processVariables);

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
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
                "warningList", processVariablesWarningValues
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";;
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );
            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
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
                "warningList", processVariablesWarningValues
            );

            when(camundaClient.getProcessInstanceVariables(S2S_TOKEN, PROCESS_INSTANCE_ID))
                .thenReturn(CamundaProcessVariables.ProcessVariablesBuilder.processVariables()
                                .withProcessVariable("warningList", WARNING_VALUES).build());

            when(externalTask.getAllVariables()).thenReturn(processVariables);
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", "[]"
            );
            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());

        }

        private NotesRequest getExpectedWarningRequest() {
            return new NotesRequest(
                singletonList(
                    new NoteResource(null, "WARNING", null, null)
                )
            );
        }

    }

    @Nested
    @DisplayName("when release 2 cft task warning disabled")
    class FeatureFlagDisabled {

        @BeforeEach
        void setUp() {
            when(launchDarklyFeatureFlagProvider.getBooleanValue(RELEASE_2_CFT_TASK_WARNING)).thenReturn(false);
        }

        @Test
        void should_complete_warning_external_task_Service() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsToBeAdded = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
            Map<String, Object> processVariables = Map.of(
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsToBeAdded
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(any(), any(), any());

        }

        @Test
        void should_complete_warning_external_task_Service_with_duplicate_warnings() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            String warningsFromHandler = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"},"
                + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

            Map<String, Object> processVariables = Map.of(
                "hasWarnings", true,
                "warningList", processVariablesWarningValues,
                "warningsToAdd", warningsFromHandler
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(any(), any(), any());

        }

        @Test
        void should_complete_warning_external_task_Service_without_warnings() {

            String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "hasWarnings", true,
                "warningList", processVariablesWarningValues
            );

            String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", expectedWarningValues
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(any(), any(), any());
        }

        @Test
        void should_complete_warning_external_task_Service_without_warning_process_variable() {

            Map<String, Object> processVariables = Map.of(
                "hasWarnings", true
            );

            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", "[]"
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);

            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(any(), any(), any());

        }

        @Test
        void should_handle_json_parsing_exception() {

            String processVariablesWarningValues = "[{\"warningCode\"\"Code1\",\"warningText\":\"Text1\"}]";
            Map<String, Object> processVariables = Map.of(
                "hasWarnings", true,
                "warningList", processVariablesWarningValues
            );

            when(externalTask.getAllVariables()).thenReturn(processVariables);
            Map<String, Object> expectedProcessVariables = Map.of(
                "hasWarnings", true,
                "warningList", "[]"
            );
            warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

            verify(externalTaskService).complete(externalTask, expectedProcessVariables);
            verify(taskManagementServiceApi, never()).addTaskNote(any(), any(), any());

        }
    }
}
