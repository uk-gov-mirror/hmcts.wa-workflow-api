package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorkerHandler;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyTaskWorkerHandlerTest {

    @Mock
    private IdempotencyTaskService idempotencyTaskService;
    @InjectMocks
    private IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler;

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;

    @Test
    void given_idempotencyKey_is_provided_then_handleIdempotencyProvidedScenario_is_called() {
        var idempotentKey = "some idempotentKey";
        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);

        var jurisdiction = "some jurisdiction";
        when(externalTask.getVariable("jurisdiction")).thenReturn(jurisdiction);

        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        verify(idempotencyTaskService).handleIdempotentIdProvidedScenario(
            externalTask,
            externalTaskService,
            new IdempotentId(idempotentKey, jurisdiction)
        );
    }

    @SuppressWarnings("checkstyle:indentation")
    @ParameterizedTest
    @CsvSource(value = {
        "null",
        "''"
    }, nullValues = {"null"})
    void given_idempotencyKey_is_not_provided_then_set_isDuplicate_to_false(String idempotentKey) {
        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);

        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskService).complete(
            externalTask,
            Collections.singletonMap("isDuplicate", false)
        );
    }

}
