package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalTaskServiceTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private ExternalTaskWorker handleWarningExternalService;

    @BeforeEach
    void setUp() {
        IdempotentKeysRepository idempotentKeysRepository = mock(IdempotentKeysRepository.class);
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
        AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

        handleWarningExternalService = new ExternalTaskWorker(
            "someUrl",
            authTokenGenerator,
            idempotentKeysRepository
        );
    }

    @Test
    void test_isDuplicate_Handler_when_false() {

        when(externalTask.getVariable("isDuplicate")).thenReturn(false);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskService).complete(externalTask);
    }

    @Test
    void test_isDuplicate_Handler_when_true() {

        when(externalTask.getVariable("isDuplicate")).thenReturn(true);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask,expectedProcessVariables);
    }

    @Test
    void test_isDuplicate_Handler_when_null() {

        when(externalTask.getVariable("isDuplicate")).thenReturn(null);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask,expectedProcessVariables);
    }
}
