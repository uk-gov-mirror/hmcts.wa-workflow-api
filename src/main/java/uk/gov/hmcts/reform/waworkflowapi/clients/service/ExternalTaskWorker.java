package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

import java.util.Map;
import java.util.logging.Logger;

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals","PMD.PositionLiteralsFirstInComparisons","PMD.LinguisticNaming"})
@Component
public class ExternalTaskWorker {

    private final String camundaUrl;

    private final AuthTokenGenerator authTokenGenerator;

    private static final Logger LOGGER = Logger.getLogger(ExternalTaskWorker.class.getName());

    public ExternalTaskWorker(
        @Value("${camunda.url}") String camundaUrl,
        AuthTokenGenerator authTokenGenerator
    ) {
        this.camundaUrl = camundaUrl;
        this.authTokenGenerator = authTokenGenerator;
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
        boolean isDuplicate =  externalTask.getVariable("isDuplicate");
        if (isDuplicate) {
            Map<String, Object> processVariables = singletonMap(
                "isDuplicate",
                false
            );

            LOGGER.info("Duplicate was hit.");

            externalTaskService.complete(externalTask, processVariables);
        } else {
            externalTaskService.complete(externalTask);
            LOGGER.info("Duplicate was true");

        }
    }
}
