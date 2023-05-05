package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.handler.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

@Profile("!functional")
@Component
@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals"})
public class ExternalTaskWorker {

    private final String camundaUrl;
    private final AuthTokenGenerator authTokenGenerator;
    private final IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler;
    private final WarningTaskWorkerHandler warningTaskWorkerHandler;

    public ExternalTaskWorker(
        @Value("${camunda.url}") String camundaUrl,
        AuthTokenGenerator authTokenGenerator,
        IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler,
        WarningTaskWorkerHandler warningTaskWorkerHandler) {

        this.camundaUrl = camundaUrl;
        this.authTokenGenerator = authTokenGenerator;
        this.idempotencyTaskWorkerHandler = idempotencyTaskWorkerHandler;
        this.warningTaskWorkerHandler = warningTaskWorkerHandler;
    }

    /*
     Because of Camunda error:
     ERROR: duplicate key value violates unique constraint "act_uniq_auth_user" from this table ACT_RU_AUTHORIZATION)
     We have to subscribe to all the external tasks from one single class.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void setupClient() {

        ExternalTaskClient idempotencyClient = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .asyncResponseTimeout(1000)
            .addInterceptor(new ServiceAuthProviderInterceptor(authTokenGenerator))
            .backoffStrategy(new ExponentialBackoffStrategy(2000L, 2, 8000L))
            .lockDuration(30000) // 30 seconds
            .build();

        idempotencyClient.subscribe("idempotencyCheck")
            .lockDuration(30000) // 30 seconds
            .handler(idempotencyTaskWorkerHandler::checkIdempotency)
            .open();


        ExternalTaskClient warningClient = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .asyncResponseTimeout(1000)
            .addInterceptor(new ServiceAuthProviderInterceptor(authTokenGenerator))
            .backoffStrategy(new ExponentialBackoffStrategy(2000L, 2, 8000L))
            .lockDuration(30000) // 30 seconds
            .build();

        warningClient.subscribe("wa-warning-topic")
            .lockDuration(30000) // 30 seconds
            .handler(warningTaskWorkerHandler::completeWarningTaskService)
            .open();

    }

}

