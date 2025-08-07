package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyKeysRepository;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyTaskServiceTest {

    @Captor
    ArgumentCaptor<IdempotencyKeys> captor;
    @Mock
    private IdempotencyKeysRepository idempotencyKeysRepository;
    @InjectMocks
    private IdempotencyTaskService idempotencyTaskService;
    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;

    @ParameterizedTest
    @CsvSource({
        "some process id,some process id,false",
        "some process id,some other process id,true",
    })
    void handleIdempotentIdIsPresentInDbTest(String processIdRow,
                                             String processIdTask,
                                             boolean isDuplicate) {
        IdempotentId idempotentId = new IdempotentId(
            "some idempotencyKey",
            "some tenant id"
        );

        when(idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotentId.getIdempotencyKey(),
            idempotentId.getTenantId()
        )).thenReturn(Optional.of(new IdempotencyKeys(
            idempotentId.getIdempotencyKey(),
            idempotentId.getTenantId(),
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

    @Test
    void handleIdempotentIdIsNotPresentInDbTest() {
        IdempotentId idempotentId = new IdempotentId(
            "some idempotencyKey",
            "some tenant id"
        );

        when(idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotentId.getIdempotencyKey(),
            idempotentId.getTenantId()
        )).thenReturn(Optional.empty());

        when(externalTask.getProcessInstanceId()).thenReturn("some process id");

        idempotencyTaskService.handleIdempotentIdProvidedScenario(
            externalTask,
            externalTaskService,
            idempotentId
        );

        verify(idempotencyKeysRepository).save(captor.capture());

        IdempotencyKeys actualIdempotentKeys = captor.getValue();
        assertThat(actualIdempotentKeys)
            .usingRecursiveComparison()
            .comparingOnlyFields("idempotencyKey", "tenantId", "processId")
            .isEqualTo(new IdempotencyKeys(
                idempotentId.getIdempotencyKey(),
                idempotentId.getTenantId(),
                "some process id",
                null,
                null
            ));

        verify(externalTaskService).complete(externalTask, singletonMap("isDuplicate", false));
    }

    @Test
    void handleFindByIdempotencyKeyAndTenantId() {
        Optional<IdempotentId> idempotentId = Optional.of(new IdempotentId(
            "some idempotencyKey",
            "some tenant id"
        ));

        LocalDateTime date = LocalDateTime.now();
        Optional<IdempotencyKeys> idempotencyKeys = Optional.of(new IdempotencyKeys(
            "some idempotencyKey",
            "some tenant id",
            "process id",
            date,
            date
        ));

        when(idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotentId.get().getIdempotencyKey(),
            idempotentId.get().getTenantId()
        )).thenReturn(idempotencyKeys);


        idempotencyTaskService.findByIdempotencyKeyAndTenantId(
            idempotencyKeys.get().getIdempotencyKey(),
            idempotencyKeys.get().getTenantId()
        );

        verify(idempotencyKeysRepository).findByIdempotencyKeyAndTenantId(
            idempotencyKeys.get().getIdempotencyKey(),
            idempotencyKeys.get().getTenantId());

    }

}
