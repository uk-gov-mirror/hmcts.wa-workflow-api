package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IdempotencyTaskWorkerHandlerTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private final IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler = new IdempotencyTaskWorkerHandler();

    @BeforeEach
    void setUp() {
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
    }

    @Test
    void test_isDuplicate_Handler_when_false() {
        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
    }

}
