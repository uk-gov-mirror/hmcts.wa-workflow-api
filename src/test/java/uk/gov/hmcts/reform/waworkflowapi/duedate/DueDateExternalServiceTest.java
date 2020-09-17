package uk.gov.hmcts.reform.waworkflowapi.duedate;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DueDateExternalServiceTest {

    private DueDateService dueDateService;
    private ExternalTask externalTask;
    private ExternalTaskService externalTaskService;
    private int workingDaysAllowed;
    private ZonedDateTime dueDate;

    @BeforeEach
    void setUp() {
        dueDateService = mock(DueDateService.class);
        externalTask = mock(ExternalTask.class);
        externalTaskService = mock(ExternalTaskService.class);
        workingDaysAllowed = 2;
        dueDate = ZonedDateTime.now();
    }

    @Test
    void testWorkingDaysHandler() {
        DueDateExternalService dueDateExternalService = new DueDateExternalService("someUrl", dueDateService);

        when(externalTask.getVariable("task")).thenReturn(singletonMap("workingDaysAllowed", workingDaysAllowed));
        when(dueDateService.addWorkingDays(workingDaysAllowed)).thenReturn(dueDate);

        dueDateExternalService.workingDaysHandler(externalTask, externalTaskService);

        Map<String, Object> expectedProcessVariables = singletonMap("overdueTaskDueDate", dueDate.format(ISO_INSTANT));
        verify(externalTaskService).complete(externalTask, expectedProcessVariables);

    }
}
