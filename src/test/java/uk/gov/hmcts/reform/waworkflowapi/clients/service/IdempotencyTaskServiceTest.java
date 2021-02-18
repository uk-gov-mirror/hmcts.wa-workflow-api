package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyTaskServiceTest {

    @Mock
    private IdempotentKeysRepository idempotentKeysRepository;
    @InjectMocks
    private IdempotencyTaskService idempotencyTaskService;

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Captor
    ArgumentCaptor<IdempotentKeys> captor;

    @ParameterizedTest
    @CsvSource({
        "some process id,some process id,false",
        "some process id,some other process id,true",
    })
    void handleIdempotentIdIsPresentInDbTest(String processIdRow,
                                             String processIdTask,
                                             boolean isDuplicate) {

        IdempotentId idempotentId = new IdempotentId(
            "some idempotentKey",
            "some tenant id"
        );

        when(idempotentKeysRepository.findById(idempotentId))
            .thenReturn(Optional.of(new IdempotentKeys(
                idempotentId,
                processIdRow,
                LocalDateTime.now(),
                LocalDateTime.now()
            )));

        when(externalTask.getProcessInstanceId()).thenReturn(processIdTask);

        idempotencyTaskService.handleIdempotentIdProvidedScenario(
            externalTask,
            externalTaskService,
            idempotentId
        );

        verify(externalTaskService).complete(externalTask, singletonMap("isDuplicate", isDuplicate));
    }

//    @Test
//    void handleIdempotentIdIsNotPresentInDbTest() {
//        when(idempotentKeysRepository.findById(idempotentId))
//            .thenReturn(Optional.empty());
//
//        when(externalTask.getProcessInstanceId()).thenReturn("some process id");
//
//        idempotencyTaskWorker.checkIdempotency(externalTask, externalTaskService);
//
//        verify(idempotentKeysRepository).save(captor.capture());
//
//        IdempotentKeys actualIdempotentKeys = captor.getValue();
//        assertThat(actualIdempotentKeys).isEqualToComparingOnlyGivenFields(
//            new IdempotentKeys(idempotentId, "some process id", null, null),
//            "idempotentId", "processId"
//        );
//
//        Map<String, Object> expectedProcessVariables = singletonMap("isDuplicate", false);
//        verify(externalTaskService).complete(externalTask, expectedProcessVariables);
//    }

}
