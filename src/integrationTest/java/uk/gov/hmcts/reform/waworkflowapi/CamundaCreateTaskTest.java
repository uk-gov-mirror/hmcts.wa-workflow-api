package uk.gov.hmcts.reform.waworkflowapi;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.of;
import static org.apache.commons.lang3.time.DateUtils.isSameDay;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.waworkflowapi.ProcessEngineBuilder.getProcessEngine;

@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.UseConcurrentHashMap" })
public class CamundaCreateTaskTest {

    public static final String PROCESS_TASK = "processTask";
    public static final String PROCESS_OVERDUE_TASK = "processOverdueTask";
    private static final String EXPECTED_GROUP = "TCW";
    private static final ZonedDateTime DUE_DATE = ZonedDateTime.now().plusDays(7);
    private static final String DUE_DATE_STRING = DUE_DATE.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    public static final Date DUE_DATE_DATE = Date.from(DUE_DATE.toInstant());

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule(getProcessEngine());

    @Test
    @Deployment(resources = { "create_task.bpmn" })
    public void createsAndCompletesATaskWithADueDate() {
        ProcessInstance processInstance = startCreateTaskProcess(of("group", EXPECTED_GROUP, "dueDate", DUE_DATE_STRING));

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey(PROCESS_TASK)
            .hasCandidateGroup(EXPECTED_GROUP)
            .hasDueDate(DUE_DATE_DATE)
            .isNotAssigned();
        assertThat(processInstance)
            .job()
            .hasDueDate(DUE_DATE_DATE);
        complete(task(PROCESS_TASK));
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = { "create_task.bpmn" })
    public void createsAndCompletesATaskWithoutADueDate() {
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put("group", EXPECTED_GROUP);
        processVariables.put("dueDate", null);
        ProcessInstance processInstance = startCreateTaskProcess(processVariables);

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey(PROCESS_TASK)
            .hasCandidateGroup(EXPECTED_GROUP)
            .isNotAssigned();

        Date dueDate = task().getDueDate();
        Date expectedDueDate = Date.from(LocalDate.now().plusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC));
        assertTrue("Expected [" + dueDate + "] to be on [" + expectedDueDate + "]", isSameDay(dueDate, expectedDueDate));

        Date jobDueDate = job().getDuedate();
        assertTrue("Expected [" + jobDueDate + "] to be on [" + expectedDueDate + "]", isSameDay(jobDueDate, expectedDueDate));
        complete(task(PROCESS_TASK));
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = { "create_task.bpmn", "getOverdueTask.dmn" })
    public void overdueAndOverdueTaskIsCreated() {
        ProcessInstance processInstance = startCreateTaskProcess(
            of("taskId", "provideRespondentEvidence", "group", EXPECTED_GROUP, "dueDate", DUE_DATE_STRING)
        );

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey(PROCESS_TASK)
            .isNotAssigned();
        execute(job());
        assertThat(processInstance).isWaitingAt(PROCESS_TASK);
        assertThat(processInstance).isWaitingAt(PROCESS_OVERDUE_TASK);
        assertThat(processInstance).task(PROCESS_OVERDUE_TASK).hasCandidateGroup("TCW");

        complete(task(PROCESS_OVERDUE_TASK));
        assertThat(processInstance).isWaitingAt(PROCESS_TASK);
        assertThat(processInstance).isNotWaitingAt(PROCESS_OVERDUE_TASK);
        assertThat(processInstance).isNotEnded();

        complete(task(PROCESS_TASK));
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = { "create_task.bpmn", "getOverdueTask.dmn" })
    public void overdueAndOverdueTaskIsNotCreated() {
        ProcessInstance processInstance = startCreateTaskProcess(
            of("taskId", "anotherTask", "group", EXPECTED_GROUP, "dueDate", DUE_DATE_STRING)
        );

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey(PROCESS_TASK)
            .isNotAssigned();

        execute(job());
        assertThat(processInstance).isWaitingAt(PROCESS_TASK);
        assertThat(processInstance).isNotWaitingAt(PROCESS_OVERDUE_TASK);

        complete(task(PROCESS_TASK));
        assertThat(processInstance).isEnded();
    }

    private ProcessInstance startCreateTaskProcess(Map<String, Object> processVariables) {
        return processEngineRule.getRuntimeService()
                .startProcessInstanceByMessage("createTaskMessage", processVariables);
    }
}
