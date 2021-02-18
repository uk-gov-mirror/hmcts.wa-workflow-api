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
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

import java.util.Optional;

import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotencyTaskService.IS_DUPLICATE;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals", "PMD.PositionLiteralsFirstInComparisons", "PMD.LinguisticNaming"})
@Service
@Slf4j
public class IdempotencyTaskWorker {

    private final String camundaUrl;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdempotencyTaskService idempotencyTaskService;

    public IdempotencyTaskWorker(
        @Value("${camunda.url}") String camundaUrl,
        AuthTokenGenerator authTokenGenerator,
        IdempotencyTaskService idempotencyTaskService) {

        this.camundaUrl = camundaUrl;
        this.authTokenGenerator = authTokenGenerator;
        this.idempotencyTaskService = idempotencyTaskService;

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
            id -> idempotencyTaskService.handleIdempotentIdProvidedScenario(externalTask, externalTaskService, id),
            () -> externalTaskService.complete(externalTask, singletonMap(IS_DUPLICATE, false))
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


}
