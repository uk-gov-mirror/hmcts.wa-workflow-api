package uk.gov.hmcts.reform.waworkflowapi.duedate;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.config.ServiceAuthProviderInterceptor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Collections.singletonMap;

@SuppressWarnings({"PMD.UseUnderscoresInNumericLiterals"})
@Component
public class DueDateExternalService {
    private static final Logger LOGGER = Logger.getLogger(DueDateExternalService.class.getName());

    private final String camundaUrl;
    private final DueDateService dueDateService;

    @Autowired
    private ServiceAuthProviderInterceptor serviceAuthProviderInterceptor;

    public DueDateExternalService(
        @Value("${camunda.url}") String camundaUrl,
        DueDateService dueDateService
    ) {
        this.camundaUrl = camundaUrl;
        this.dueDateService = dueDateService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setupClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(camundaUrl)
            .asyncResponseTimeout(10000)
            .addInterceptor(serviceAuthProviderInterceptor)
            .build();

        client.subscribe("calculate-due-date")
            .lockDuration(1000)
            .handler(this::workingDaysHandler)
            .open();
    }

    public void workingDaysHandler(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        int workingDaysAllowed = (int) ((Map<?, ?>) externalTask
            .getVariable("task")).get("workingDaysAllowed");

        ZonedDateTime dueDate = dueDateService.addWorkingDays(workingDaysAllowed);

        Map<String, Object> processVariables = singletonMap(
            "overdueTaskDueDate",
            dueDate.format(DateTimeFormatter.ISO_INSTANT)
        );

        LOGGER.info("Overdue task has due date of " + dueDate.format(DateTimeFormatter.ISO_INSTANT));
        externalTaskService.complete(externalTask, processVariables);
    }
}
