package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningTaskWorkerHandlerTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private final WarningTaskWorkerHandler warningTaskWorkerHandler = new WarningTaskWorkerHandler();


    @BeforeEach
    void setUp() {
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
    }

    @Test
    void test_HasWarning_Handler_when_false() {
        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", false));

        warningTaskWorkerHandler.checkHasWarnings(externalTask, externalTaskService);

        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask, processVariables);
    }

    @Test
    void test_HasWarning_Handler_when_true() {
        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", true));

        warningTaskWorkerHandler.checkHasWarnings(externalTask, externalTaskService);
        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask, processVariables);
    }

    @Test
    void test_HasWarning_Handler_when_empty() {
        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", null));

        warningTaskWorkerHandler.checkHasWarnings(externalTask, externalTaskService);

        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask, processVariables);
    }

}
