package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExternalTaskServiceTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;


    @BeforeEach
    void setUp() {
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
    }

    @Test
    void test_isDuplicate_Handler_when_false() {
        ExternalTaskWorker handleWarningExternalService = new ExternalTaskWorker("someUrl", authTokenGenerator);

        when(externalTask.getVariable("isDuplicate")).thenReturn(false);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void test_isDuplicate_Handler_when_true() {
        ExternalTaskWorker handleWarningExternalService = new ExternalTaskWorker("someUrl", authTokenGenerator);

        when(externalTask.getVariable("isDuplicate")).thenReturn(true);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask,expectedProcessVariables);
    }
}
