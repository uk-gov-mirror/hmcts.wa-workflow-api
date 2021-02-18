package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class HandleWarningExternalServiceTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private HandleWarningExternalService mocHandleWarningExternalService;


    @BeforeEach
    void setUp() {
        authTokenGenerator = mock(AuthTokenGenerator.class);
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
    }

    @Test
    void test_HasWarning_Handler_when_false() {
        HandleWarningExternalService handleWarningExternalService = new HandleWarningExternalService("someUrl", authTokenGenerator);

        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", false));

        handleWarningExternalService.checkHasWarnings(externalTask, externalTaskService);

        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask,processVariables);
    }

    @Test
    void test_HasWarning_Handler_when_true() {
        HandleWarningExternalService handleWarningExternalService = new HandleWarningExternalService("someUrl", authTokenGenerator);

        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", true));

        handleWarningExternalService.checkHasWarnings(externalTask, externalTaskService);
        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask,processVariables);
    }

    @Test
    void test_HasWarning_Handler_when_empty() {
        HandleWarningExternalService handleWarningExternalService = new HandleWarningExternalService("someUrl", authTokenGenerator);

        when(externalTask.getAllVariables()).thenReturn(singletonMap("hasWarnings", null));

        handleWarningExternalService.checkHasWarnings(externalTask, externalTaskService);

        Map<String, Object> processVariables = singletonMap(
            "hasWarnings",
            true
        );
        verify(externalTaskService).complete(externalTask,processVariables);
    }

    @Test
    void test_HasWarning_Handler_when_check_setup() {
        mocHandleWarningExternalService.setupClient();
        mocHandleWarningExternalService.checkHasWarnings(externalTask, externalTaskService);

        verify(mocHandleWarningExternalService, times(1)).setupClient();
        verify(mocHandleWarningExternalService, times(1)).checkHasWarnings(externalTask,externalTaskService);
    }
}
