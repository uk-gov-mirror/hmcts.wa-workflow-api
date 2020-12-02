package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

class SendMessageServiceTest {

    private TaskClientService taskClientService;
    private ServiceDetails serviceDetails;
    private SendMessageRequest sendMessageRequest;


    @BeforeEach
    void setUp() {
        serviceDetails = new ServiceDetails("jurisdiction", "caseType");
        taskClientService = mock(TaskClientService.class);
        serviceDetails = new ServiceDetails("some_jurisdiction", "some_case_type");
        sendMessageRequest = new SendMessageRequest("createTaskMessage", null);
    }

    //@Test
    //void createsATask() {
    //    sendMessageService.createMessage(sendMessageRequest);
    //    assertEquals(evaluateDmn.get(0).get("test").getValue(), "TestResponse");
    //    verify(taskClientService).evaluate(evaluateDmnRequest, "test");
    //}


    private List<Map<String,DmnValue>> mockResponse() {
        return List.of(Map.of("test",DmnValue.dmnStringValue("TestResponse")));
    }

}
