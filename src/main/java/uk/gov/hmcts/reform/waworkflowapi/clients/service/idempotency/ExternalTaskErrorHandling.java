package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Component
public interface ExternalTaskErrorHandling {

    void handleError(ExternalTask externalTask, ExternalTaskService externalTaskService, Exception exception);

}
