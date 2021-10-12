package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.TaskManagementServiceApi;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.handler.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag.RELEASE_2_CFT_TASK_WARNING;


@ExtendWith(MockitoExtension.class)
class WarningTaskWorkerHandlerTest {

    private static final String S2S_TOKEN = "some S2SToken";
    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private TaskManagementServiceApi taskManagementServiceApi;
    @Mock
    private LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;
    private WarningTaskWorkerHandler warningTaskWorkerHandler;

    @BeforeEach
    void setUp() {
        warningTaskWorkerHandler = new WarningTaskWorkerHandler(
            taskManagementServiceApi,
            authTokenGenerator,
            launchDarklyFeatureFlagProvider
        );

        lenient().when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Nested
    @DisplayName("when release 2 cft task warning enabled")
    class FeatureFlagEnabled {

        @BeforeEach
        void setUp() {
            when(launchDarklyFeatureFlagProvider.getBooleanValue(RELEASE_2_CFT_TASK_WARNING)).thenReturn(true);
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
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());

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
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());

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
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());
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
            verify(taskManagementServiceApi).addTaskNote(S2S_TOKEN, externalTask.getId(), getExpectedWarningRequest());

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
