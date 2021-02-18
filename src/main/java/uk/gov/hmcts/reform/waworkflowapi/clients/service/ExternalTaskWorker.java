package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals", "PMD.PositionLiteralsFirstInComparisons", "PMD.LinguisticNaming"})
@Service
@Slf4j
public class ExternalTaskWorker {

    public static final String IS_DUPLICATE = "isDuplicate";
    private final String camundaUrl;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdempotentKeysRepository idempotentKeysRepository;

    public ExternalTaskWorker(
        @Value("${camunda.url}") String camundaUrl,
        AuthTokenGenerator authTokenGenerator,
        IdempotentKeysRepository idempotentKeysRepository
    ) {
        this.camundaUrl = camundaUrl;
        this.authTokenGenerator = authTokenGenerator;
        this.idempotentKeysRepository = idempotentKeysRepository;
    }


    @EventListener(ApplicationReadyEvent.class)
    public void setupClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .addInterceptor(new ServiceAuthProviderInterceptor(authTokenGenerator))
            .build();

        client.subscribe("idempotencyCheck")
            .lockDuration(1000)
            .handler(this::checkIdempotency)
            .open();
    }

    public void checkIdempotency(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Optional<IdempotentId> idempotentId = getIdempotentId(externalTask);
        idempotentId.ifPresentOrElse(
            id -> handleIdempotentIdProvidedScenario(externalTask, externalTaskService, id),
            () -> externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false))
        );
    }

    private void handleIdempotentIdProvidedScenario(ExternalTask externalTask,
                                                    ExternalTaskService externalTaskService,
                                                    IdempotentId idempotentId) {
        Optional<IdempotentKeys> idempotentRow = idempotentKeysRepository.findById(idempotentId);

        idempotentRow.ifPresentOrElse(
            (row) -> handleIdempotentIdIsPresentInDb(externalTask, externalTaskService, row),
            () -> handleIdempotentIdIsNotPresentInDb(externalTask, externalTaskService, idempotentId)
        );
    }

    private Optional<IdempotentId> getIdempotentId(ExternalTask externalTask) {
        String idempotentKey = externalTask.getVariable("idempotentKey");
        if (StringUtils.isNotBlank(idempotentKey)) {
            String tenantId = externalTask.getVariable("jurisdiction");
            return Optional.of(new IdempotentId(idempotentKey, tenantId));
        }
        return Optional.empty();
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
