package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalTaskServiceTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private ExternalTaskWorker handleWarningExternalService;
    private IdempotentKeysRepository idempotentKeysRepository;

    @BeforeEach
    void setUp() {
        idempotentKeysRepository = mock(IdempotentKeysRepository.class);
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
    void test_isDuplicate_Handler_when_true() {

        when(idempotentKeysRepository.findById(new IdempotentId(externalTask.getVariable("idempotentKey"),"ia")))
            .thenReturn(Optional.of(new IdempotentKeys(
            new IdempotentId(UUID.randomUUID().toString(), "ia"),
            "processId",
            LocalDateTime.now(),
            LocalDateTime.now()
        )));

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);
        Map<String, Object> processVariables = singletonMap("isDuplicate", true);
        verify(externalTaskService).complete(externalTask,processVariables);
    }

    @Test
    void test_isDuplicate_Handler_when_false() {
        when(idempotentKeysRepository.findById(new IdempotentId(externalTask.getVariable("idempotentKey"),"ia")))
            .thenReturn(Optional.empty());


        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask,expectedProcessVariables);
    }
}
