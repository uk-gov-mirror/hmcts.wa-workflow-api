package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonMap;

@Component
@Slf4j
public class IdempotencyTaskService {

    public static final String IS_DUPLICATE = "isDuplicate";

    private final IdempotencyKeysRepository idempotencyKeysRepository;

    public IdempotencyTaskService(IdempotencyKeysRepository idempotencyKeysRepository) {
        this.idempotencyKeysRepository = idempotencyKeysRepository;
    }

    public void handleIdempotentIdProvidedScenario(ExternalTask externalTask,
                                                   ExternalTaskService externalTaskService,
                                                   IdempotentId idempotentId) {
        Optional<IdempotencyKeys> idempotentRow = idempotencyKeysRepository.findById(idempotentId);

        idempotentRow.ifPresentOrElse(
            (row) -> handleIdempotentIdIsPresentInDb(externalTask, externalTaskService, row),
            () -> handleIdempotentIdIsNotPresentInDb(externalTask, externalTaskService, idempotentId)
        );
    }

    private void handleIdempotentIdIsNotPresentInDb(ExternalTask externalTask,
                                                    ExternalTaskService externalTaskService,
                                                    IdempotentId idempotentId) {
        log.info("idempotencyKey({}) does not exist in the database.", idempotentId);
        idempotencyKeysRepository.save(new IdempotencyKeys(
            idempotentId,
            externalTask.getProcessInstanceId(),
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
        externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
    }

    private void handleIdempotentIdIsPresentInDb(ExternalTask externalTask,
                                                 ExternalTaskService externalTaskService,
                                                 IdempotencyKeys row) {
        log.info("idempotencyKey({}) already exists in the database.", row.getIdempotentId());
        if (isSameProcessId(externalTask, row)) {
            externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
        } else {
            externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, true));
        }
    }

    private boolean isSameProcessId(ExternalTask externalTask, IdempotencyKeys row) {
        return externalTask.getProcessInstanceId().equals(row.getProcessId());
    }

}
