package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

@Component
@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals"})
public class ExternalTaskWorker {

    public static final int LOCK_DURATION = 30000;
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
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .addInterceptor(new ServiceAuthProviderInterceptor(authTokenGenerator))
            .asyncResponseTimeout(10000)
            .build();

        client.subscribe("wa-warning-topic")
            .lockDuration(LOCK_DURATION)
            .handler(warningTaskWorkerHandler::checkHasWarnings)
            .open();

        client.subscribe("idempotencyCheck")
            .lockDuration(LOCK_DURATION)
            .handler(idempotencyTaskWorkerHandler::checkIdempotency)
            .open();
    }


}
