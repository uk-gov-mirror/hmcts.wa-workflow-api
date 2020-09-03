package uk.gov.hmcts.reform.waworkflowapi;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static java.util.Map.of;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static uk.gov.hmcts.reform.waworkflowapi.ProcessEngineBuilder.getProcessEngine;

public class CamundaCreateTaskTest {

    public static final String PROCESS_TASK = "processTask";
    public static final String PROCESS_OVERDUE_TASK = "processOverdueTask";
    private static final String EXPECTED_GROUP = "TCW";

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule(getProcessEngine());

    @Test
    @Deployment(resources = { "create_task.bpmn" })
    public void createsAndCompletesATask() {
        ProcessInstance processInstance = startCreateTaskProcess(of("group", EXPECTED_GROUP));

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey(PROCESS_TASK)
            .hasCandidateGroup(EXPECTED_GROUP)
            .isNotAssigned();
        complete(task(PROCESS_TASK));
        assertThat(processInstance).isEnded();
    }

    @Test
    @Deployment(resources = { "create_task.bpmn", "getOverdueTask.dmn" })
    public void overdueAndOverdueTaskIsCreated() {
        ProcessInstance processInstance = startCreateTaskProcess(
            of("taskId", "uploadRespondentEvidence", "group", EXPECTED_GROUP)
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
        ProcessInstance processInstance = startCreateTaskProcess(of("taskId", "anotherTask", "group", EXPECTED_GROUP));

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
