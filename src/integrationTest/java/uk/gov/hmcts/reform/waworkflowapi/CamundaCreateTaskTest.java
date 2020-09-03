package uk.gov.hmcts.reform.waworkflowapi;

import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;

public class CamundaCreateTaskTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule(new StandaloneInMemProcessEngineConfiguration().buildProcessEngine());

    @Test
    @Deployment(resources = { "create_task.bpmn" })
    public void ruleUsageExample() {
        ProcessInstance processInstance = processEngineRule.getRuntimeService().startProcessInstanceByMessage("createTaskMessage");

        assertThat(processInstance).isStarted()
            .task()
            .hasDefinitionKey("processTask")
            .isNotAssigned();
    }
}
