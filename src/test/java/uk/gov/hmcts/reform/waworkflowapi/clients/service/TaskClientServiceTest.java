package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskClientServiceTest {

    private CamundaClient camundaClient;
    private TaskClientService underTest;
    private AuthTokenGenerator authTokenGenerator;
    private EvaluateDmnRequest evaluateDmnRequest;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    private List<Map<String, DmnValue<?>>> mockResponse() {
        return List.of(Map.of("test",DmnValue.dmnStringValue("TestValue")));
    }

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        authTokenGenerator = mock(AuthTokenGenerator.class);
        underTest = new TaskClientService(camundaClient, authTokenGenerator);
        evaluateDmnRequest = new EvaluateDmnRequest(Map.of("name",DmnValue.dmnStringValue("test")));
        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
    }

    @Test
    void evaluateDmnRequest() {
        when(camundaClient.evaluateDmn(
            eq(BEARER_SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(evaluateDmnRequest)
        )).thenReturn(mockResponse());

        List<Map<String,DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");
        assertEquals(task.get(0).get("test").getValue(), "TestValue");
    }

    @Test
    void evaluateForEmptyDmn() {
        List<Map<String,DmnValue<?>>> ts = emptyList();
        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            "key",
            "id",
            evaluateDmnRequest
        )).thenReturn(ts);

        List<Map<String,DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");

        assertEquals(task, new ArrayList<>());
    }
}
