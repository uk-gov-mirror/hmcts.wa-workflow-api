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
        log.info("checking if idempotentId({}) is present in DB...", idempotentId);
        Optional<IdempotencyKeys> idempotentRow = idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotentId.getIdempotencyKey(),
            idempotentId.getTenantId()
        );

        idempotentRow.ifPresentOrElse(
            (row) -> handleIdempotentIdIsPresentInDb(externalTask, externalTaskService, row),
            () -> handleIdempotentIdIsNotPresentInDb(externalTask, externalTaskService, idempotentId)
        );
    }

    public Optional<IdempotencyKeys> findByIdempotencyKeyAndTenantId(String idempotencyKey, String jurisdiction) {

        return idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotencyKey,
            jurisdiction
        );

    }

    private void handleIdempotentIdIsNotPresentInDb(ExternalTask externalTask,
                                                    ExternalTaskService externalTaskService,
                                                    IdempotentId idempotentId) {
        log.info("Saving idempotentId({}) in the DB because is not present", idempotentId);
        idempotencyKeysRepository.save(new IdempotencyKeys(
            idempotentId.getIdempotencyKey(),
            idempotentId.getTenantId(),
            externalTask.getProcessInstanceId(),
            LocalDateTime.now(),
            LocalDateTime.now()
        ));
        externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
    }

    private void handleIdempotentIdIsPresentInDb(ExternalTask externalTask,
                                                 ExternalTaskService externalTaskService,
                                                 IdempotencyKeys row) {
        log.info(
            "Not saving idempotentId({}) because already exists in the database.",
            new IdempotentId(row.getIdempotencyKey(), row.getTenantId())
        );
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
