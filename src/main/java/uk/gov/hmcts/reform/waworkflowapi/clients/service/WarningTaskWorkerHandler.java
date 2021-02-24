package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
public class WarningTaskWorkerHandler {

    public void checkHasWarnings(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        Map<?, ?> variables = externalTask.getAllVariables();
        var hasWarnings = variables.get("hasWarnings");
        if (hasWarnings == null) {
            Map<String, Object> processVariables = singletonMap(
                "hasWarnings",
                true
            );
            externalTaskService.complete(externalTask, processVariables);
        } else {
            externalTaskService.complete(externalTask, singletonMap(
                "hasWarnings",
                true
            ));

        }
    }
}

