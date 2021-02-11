package uk.gov.hmcts.reform.waworkflowapi.clients.service;

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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals","PMD.PositionLiteralsFirstInComparisons","PMD.LinguisticNaming"})
@Service
public class ExternalTaskWorker {

    private final String camundaUrl;

    private final AuthTokenGenerator authTokenGenerator;

    private final IdempotentKeysRepository idempotentKeysRepository;

    private static final Logger LOGGER = Logger.getLogger(ExternalTaskWorker.class.getName());

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
        Optional<IdempotentKeys> existingTask = idempotentKeysRepository.findById(new IdempotentId(externalTask.getVariable("idempotentKey"),"ia"));

        if (existingTask.isPresent()) {
            Map<String, Object> processVariables = singletonMap(
                "isDuplicate",
                true
            );
            LOGGER.info("is already in the database.");
            externalTaskService.complete(externalTask, processVariables);
        } else {
            LOGGER.info("Saving new id to database");
            IdempotentKeys keys = new IdempotentKeys(
                new IdempotentId(UUID.randomUUID().toString(), "ia"),
                "processId",
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            idempotentKeysRepository.save(keys);
            Map<String, Object> processVariables = singletonMap(
                "isDuplicate",
                false
            );
            externalTaskService.complete(externalTask,processVariables);
        }
    }
}
