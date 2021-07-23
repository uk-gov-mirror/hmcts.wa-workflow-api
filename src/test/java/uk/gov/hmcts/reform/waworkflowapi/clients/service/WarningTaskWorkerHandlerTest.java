package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.handler.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class WarningTaskWorkerHandlerTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private WarningTaskWorkerHandler warningTaskWorkerHandler;
    private LaunchDarklyFeatureToggler featureToggler;

    @BeforeEach
    void setUp() {
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
        featureToggler = mock(LaunchDarklyFeatureToggler.class);
        warningTaskWorkerHandler = new WarningTaskWorkerHandler(featureToggler);
    }

    @Test
    void should_complete_warning_external_task_Service() {

        String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
        Map<String, Object> processVariables = Map.of(
            "hasWarnings", true,
            "warningList", processVariablesWarningValues,
            "warningCode", "Code2",
            "warningText", "Text2"
        );

        String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"},"
            + "{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
        Map<String, Object> expectedProcessVariables = Map.of(
            "hasWarnings", true,
            "warningList", expectedWarningValues
        );

        when(externalTask.getAllVariables()).thenReturn(processVariables);

        warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
    }

    @Test
    void should_complete_non_iac_warning_external_task_Service() {

        when(featureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false)).thenReturn(true);
        String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
        String warningsFromHandler = "[{\"warningCode\":\"Code2\",\"warningText\":\"Text2\"}]";
        Map<String, Object> processVariables = Map.of(
            "hasWarnings", true,
            "warningList", processVariablesWarningValues,
            "warnings",  warningsFromHandler
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
    }

    @Test
    void should_complete_non_iac_warning_external_task_Service_with_duplicate_warnings() {

        when(featureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false)).thenReturn(true);
        String processVariablesWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
        String warningsFromHandler = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"},"
            + "{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";

        Map<String, Object> processVariables = Map.of(
            "hasWarnings", true,
            "warningList", processVariablesWarningValues,
            "warnings",  warningsFromHandler
        );

        String expectedWarningValues = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
        Map<String, Object> expectedProcessVariables = Map.of(
            "hasWarnings", true,
            "warningList", expectedWarningValues
        );

        when(externalTask.getAllVariables()).thenReturn(processVariables);

        warningTaskWorkerHandler.completeWarningTaskService(externalTask, externalTaskService);

        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
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
    }

    @Test
    void should_complete_non_iac_warning_external_task_Service_without_warnings() {

        when(featureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false)).thenReturn(true);
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
    }

    @Test
    void should_complete_non_iac_warning_external_task_Service_without_warning_process_variable() {

        when(featureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false)).thenReturn(true);
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
    }

}
