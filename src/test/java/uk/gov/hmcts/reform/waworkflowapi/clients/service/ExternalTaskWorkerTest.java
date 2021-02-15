package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalTaskWorkerTest {

    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private ExternalTaskWorker handleWarningExternalService;
    private IdempotentKeysRepository idempotentKeysRepository;
    private IdempotentId idempotentId;
    @Captor
    ArgumentCaptor<IdempotentKeys> captor;

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

        String idempotentKey = "some idempotent key";
        when(externalTask.getVariable("idempotentKey")).thenReturn(idempotentKey);

        String jurisdiction = "some jurisdiction";
        when(externalTask.getVariable("jurisdiction")).thenReturn(jurisdiction);

        idempotentId = new IdempotentId(idempotentKey, jurisdiction);
    }

    @ParameterizedTest
    @CsvSource({
        "some process id,some process id,false",
        "some process id,some other process id,true",
    })
    void handleIdempotentIdIsPresentInDbTest(String processIdRow, String processIdTask, boolean isDuplicate) {

        when(idempotentKeysRepository.findById(idempotentId))
            .thenReturn(Optional.of(new IdempotentKeys(
                idempotentId,
                processIdRow,
                LocalDateTime.now(),
                LocalDateTime.now()
            )));

        when(externalTask.getProcessInstanceId()).thenReturn(processIdTask);

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        Map<String, Object> processVariables = singletonMap("isDuplicate", isDuplicate);
        verify(externalTaskService).complete(externalTask, processVariables);
    }

    @Test
    void handleIdempotentIdIsNotPresentInDbTest() {
        when(idempotentKeysRepository.findById(idempotentId))
            .thenReturn(Optional.empty());

        when(externalTask.getProcessInstanceId()).thenReturn("some process id");

        handleWarningExternalService.checkIdempotency(externalTask, externalTaskService);

        verify(idempotentKeysRepository).save(captor.capture());

        IdempotentKeys actualIdempotentKeys = captor.getValue();
        assertThat(actualIdempotentKeys).isEqualToComparingOnlyGivenFields(
            new IdempotentKeys(idempotentId, "some process id", null, null),
            "idempotentId", "processId"
        );

        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
    }
}
