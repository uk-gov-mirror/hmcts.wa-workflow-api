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

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals","PMD.PositionLiteralsFirstInComparisons","PMD.LinguisticNaming"})
@Component
public class HandleWarningExternalService {

    private final String camundaUrl;

    private final AuthTokenGenerator authTokenGenerator;

    public HandleWarningExternalService(
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
            .asyncResponseTimeout(10000)
            .build();

        client.subscribe("warning-topic")
            .lockDuration(1000)
            .handler(this::checkHasWarnings)
            .open();
    }

    public void checkHasWarnings(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        var hasWarnings =  ((Map<?, ?>) externalTask.getVariable("task")).get("hasWarnings");

        if (hasWarnings != null && hasWarnings.equals(false)) {
            Map<String, Object> processVariables = singletonMap(
                "hasWarnings",
                true
            );
            externalTaskService.complete(externalTask, processVariables);
        }

        externalTaskService.complete(externalTask);
    }
}
