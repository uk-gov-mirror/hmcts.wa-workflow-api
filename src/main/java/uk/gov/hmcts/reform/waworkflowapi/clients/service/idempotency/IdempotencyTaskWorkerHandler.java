package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;

import java.util.Optional;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskService.IS_DUPLICATE;

@Service
@Slf4j
public class IdempotencyTaskWorkerHandler {

    private final IdempotencyTaskService idempotencyTaskService;

    public IdempotencyTaskWorkerHandler(
        IdempotencyTaskService idempotencyTaskService) {
        this.idempotencyTaskService = idempotencyTaskService;
    }

    public void checkIdempotency(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Optional<IdempotentId> idempotentId = getIdempotentId(externalTask);
        idempotentId.ifPresentOrElse(
            id -> idempotencyTaskService.handleIdempotentIdProvidedScenario(externalTask, externalTaskService, id),
            () -> completeTask(externalTask, externalTaskService)
        );
    }

    private void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String msg = "No idempotencyKey found for process instance({}), "
                     + "probably a service other than wa/ia is using the BPM.";
        log.info(msg, externalTask.getProcessInstanceId());
        externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
    }

    private Optional<IdempotentId> getIdempotentId(ExternalTask externalTask) {
        String idempotencyKey = externalTask.getVariable("idempotencyKey");
        if (StringUtils.isNotBlank(idempotencyKey)) {
            String tenantId = externalTask.getVariable("jurisdiction");
            return Optional.of(new IdempotentId(idempotencyKey, tenantId));
        }
        return Optional.empty();
    }


}
