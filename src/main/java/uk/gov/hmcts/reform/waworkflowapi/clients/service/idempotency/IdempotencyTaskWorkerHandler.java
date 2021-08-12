package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
    private final ExternalTaskErrorHandling externalTaskErrorHandlingWithThreeRetries;

    public IdempotencyTaskWorkerHandler(IdempotencyTaskService idempotencyTaskService,
                                        ExternalTaskErrorHandling externalTaskErrorHandlingWithThreeRetries) {
        this.idempotencyTaskService = idempotencyTaskService;
        this.externalTaskErrorHandlingWithThreeRetries = externalTaskErrorHandlingWithThreeRetries;
    }

    public void checkIdempotency(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("checking idempotency...");
        try {
            Optional<IdempotentId> idempotentId = getIdempotentId(externalTask);
            idempotentId.ifPresentOrElse(
                id -> idempotencyTaskService.handleIdempotentIdProvidedScenario(externalTask, externalTaskService, id),
                () -> completeTask(externalTask, externalTaskService)
            );
        } catch (Exception e) {
            externalTaskErrorHandlingWithThreeRetries.handleError(externalTask, externalTaskService, e);
        }
    }

    private void completeTask(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        String msg = "No idempotencyKey found for process instance({}), "
            + "probably a service other than wa/ia is using the BPM.";
        log.info(msg, externalTask.getProcessInstanceId());
        externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false));
    }

    private Optional<IdempotentId> getIdempotentId(ExternalTask externalTask) {
        String idempotencyKey = externalTask.getVariable("idempotencyKey");
        String tenantId = externalTask.getVariable("jurisdiction");
        if (StringUtils.isNotBlank(idempotencyKey) && StringUtils.isNotBlank(tenantId)) {
            log.info("build idempotentId with key({}) and tenantId({})...", idempotencyKey, tenantId);
            return Optional.of(new IdempotentId(idempotencyKey, tenantId));
        }
        log.info("idempotentId is blank");
        return Optional.empty();
    }

}
