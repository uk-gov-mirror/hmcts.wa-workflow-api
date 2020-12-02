package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluateDmnServiceTest {

    private TaskClientService taskClientService;
    private EvaluateDmnService evaluateDmnService;
    private ServiceDetails serviceDetails;
    private EvaluateDmnRequest evaluateDmnRequest;


    @BeforeEach
    void setUp() {
        serviceDetails = new ServiceDetails("jurisdiction", "caseType");
        evaluateDmnRequest = new EvaluateDmnRequest(Map.of("name",DmnValue.dmnStringValue("test")),serviceDetails);
        taskClientService = mock(TaskClientService.class);
        evaluateDmnService = new EvaluateDmnService(taskClientService);
    }

    @Test
    void createsATask() {
        when(taskClientService.evaluate(evaluateDmnRequest,"test")).thenReturn(mockResponse());
        List<Map<String,DmnValue>> evaluateDmn = evaluateDmnService.evaluateDmn(evaluateDmnRequest,"test");

        assertEquals(evaluateDmn.get(0).get("test").getValue(), "TestResponse");
        verify(taskClientService).evaluate(evaluateDmnRequest, "test");
    }


    private List<Map<String,DmnValue>> mockResponse() {
        return List.of(Map.of("test",DmnValue.dmnStringValue("TestResponse")));
    }

}
