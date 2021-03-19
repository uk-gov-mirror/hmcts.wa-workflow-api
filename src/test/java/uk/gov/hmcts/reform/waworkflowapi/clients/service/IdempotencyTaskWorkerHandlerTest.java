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
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.ExternalTaskErrorHandling;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorkerHandler;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyTaskWorkerHandlerTest {

    @Mock
    private IdempotencyTaskService idempotencyTaskService;
    @Mock
    private ExternalTaskErrorHandling externalTaskErrorHandlingWithThreeRetries;
    @InjectMocks
    private IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler;

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;

    @Test
    void given_exception_then_handle_error() {
        given(externalTask.getVariable(anyString())).willThrow(RuntimeException.class);

        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskErrorHandlingWithThreeRetries).handleError(
            eq(externalTask),
            eq(externalTaskService),
            any(RuntimeException.class)
        );
    }

    @Test
    void given_idempotencyId_is_provided_then_handleIdempotencyProvidedScenario_is_called() {
        String idempotencyKey = "some idempotency key";
        when(externalTask.getVariable("idempotencyKey")).thenReturn(idempotencyKey);

        String tenantId = "some tenant id";
        when(externalTask.getVariable("jurisdiction")).thenReturn(tenantId);

        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        verify(idempotencyTaskService).handleIdempotentIdProvidedScenario(
            externalTask,
            externalTaskService,
            new IdempotentId(idempotencyKey, tenantId)
        );
    }

    @SuppressWarnings("checkstyle:indentation")
    @ParameterizedTest
    @CsvSource(value = {
        "null, some tenantId",
        "'', some tenantId",
        "some idempotencyKey, null",
        "some idempotencyKey, ''"
    }, nullValues = {"null"})
    void given_idempotencyKey_is_not_provided_then_set_isDuplicate_to_false(String idempotencyKey, String tenantId) {
        when(externalTask.getVariable("idempotencyKey")).thenReturn(idempotencyKey);
        when(externalTask.getVariable("jurisdiction")).thenReturn(tenantId);

        idempotencyTaskWorkerHandler.checkIdempotency(externalTask, externalTaskService);

        verify(externalTaskService).complete(
            externalTask,
            Collections.singletonMap("isDuplicate", false)
        );
    }

}
