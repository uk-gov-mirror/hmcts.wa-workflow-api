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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorker;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyTaskWorkerTest {

    @Mock
    private IdempotencyTaskService idempotencyTaskService;
    @InjectMocks
    private IdempotencyTaskWorker idempotencyTaskWorker;

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;

    @Test
    void given_idempotencyKey_is_provided_then_handleIdempotencyProvidedScenario_is_called() {
        String idempotentKey = "some idempotentKey";
        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);

        String jurisdiction = "some jurisdiction";
        when(externalTask.getVariable("jurisdiction")).thenReturn(jurisdiction);

        idempotencyTaskWorker.checkIdempotency(externalTask, externalTaskService);

        verify(idempotencyTaskService).handleIdempotentIdProvidedScenario(
            externalTask,
            externalTaskService,
            new IdempotentId(idempotentKey, jurisdiction)
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
        "null",
        "''"
    }, nullValues = {"null"})
    void given_idempotencyKey_is_not_provided_then_set_isDuplicate_to_false(String idempotentKey) {
        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);

        idempotencyTaskWorker.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskService).complete(
            externalTask,
            Collections.singletonMap("isDuplicate", false)
        );
    }

}
