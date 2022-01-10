package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables.ProcessVariablesBuilder.processVariables;

class CamundaProcessVariablesTest {

    @Test
    void should_set_properties() {

        CamundaProcessVariables testObject = processVariables()
            .withProcessVariable("caseId", "0000000")
            .withProcessVariable("taskId", "someTaskId")
            .withProcessVariable("dueDate", "2020-09-27")
            .withProcessVariableBoolean("unknown", true)
            .withProcessVariable("warningList", (new WarningValues(Collections.emptyList())).toString())
            .withProcessVariable("caseManagementCategory", "Protection")
            .build();

        assertEquals(new DmnValue<>("0000000", "String"), testObject.getProcessVariablesMap().get("caseId"));
        assertEquals(new DmnValue<>("someTaskId", "String"), testObject.getProcessVariablesMap().get("taskId"));
        assertEquals(new DmnValue<>("2020-09-27", "String"), testObject.getProcessVariablesMap().get("dueDate"));
        assertEquals(new DmnValue<>(true, "boolean"), testObject.getProcessVariablesMap().get("unknown"));

        String wv = (new WarningValues(Collections.emptyList())).toString();
        assertEquals(new DmnValue<>(wv, "String"), testObject.getProcessVariablesMap().get("warningList"));
        assertEquals(6, testObject.getProcessVariablesMap().size());
        DmnValue category = new DmnValue<>("Protection", "String");
        assertEquals(category, testObject.getProcessVariablesMap().get("caseManagementCategory"));
    }

}
