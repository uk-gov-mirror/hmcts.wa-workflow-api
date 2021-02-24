package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
public class IdempotencyTaskWorkerHandler {

    public void checkIdempotency(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<String, Object> processVariables = singletonMap(
            "isDuplicate",
            false
        );
        externalTaskService.complete(externalTask, processVariables);
    }

}
