package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonMap;

@Component
@Slf4j
public class IdempotencyTaskService {

    public static final String IS_DUPLICATE = "isDuplicate";

    private final IdempotentKeysRepository idempotentKeysRepository;

    public IdempotencyTaskService(IdempotentKeysRepository idempotentKeysRepository) {
        this.idempotentKeysRepository = idempotentKeysRepository;
    }

    public void handleIdempotentIdProvidedScenario(ExternalTask externalTask,
                                                   ExternalTaskService externalTaskService,
                                                   IdempotentId idempotentId) {
        Optional<IdempotentKeys> idempotentRow = idempotentKeysRepository.findById(idempotentId);

        idempotentRow.ifPresentOrElse(
            (row) -> handleIdempotentIdIsPresentInDb(externalTask, externalTaskService, row),
            () -> handleIdempotentIdIsNotPresentInDb(externalTask, externalTaskService, idempotentId)
        );
    }

    private void handleIdempotentIdIsNotPresentInDb(ExternalTask externalTask,
                                                    ExternalTaskService externalTaskService,
                                                    IdempotentId idempotentId) {
        log.info("idempotentKey({}) does not exist in the database.", idempotentId);
        idempotentKeysRepository.save(new IdempotentKeys(
            idempotentId,
            externalTask.getProcessInstanceId(),
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
        externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
    }

    private void handleIdempotentIdIsPresentInDb(ExternalTask externalTask,
                                                 ExternalTaskService externalTaskService,
                                                 IdempotentKeys row) {
        log.info("idempotentKey({}) already exists in the database.", row.getIdempotentId());
        if (isSameProcessId(externalTask, row)) {
            externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
        } else {
            externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, true));
        }
    }

    private boolean isSameProcessId(ExternalTask externalTask, IdempotentKeys row) {
        return externalTask.getProcessInstanceId().equals(row.getProcessId());
    }

}
