package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskClientServiceTest {

    private CamundaClient camundaClient;
    private TaskClientService underTest;
    private ServiceDetails serviceDetails;
    private AuthTokenGenerator authTokenGenerator;
    private EvaluateDmnRequest evaluateDmnRequest;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    private List<Map<String,Object>> mockResponse() {
        return List.of(Map.of("test",DmnValue.dmnStringValue("TestValue")));
    }

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        authTokenGenerator = mock(AuthTokenGenerator.class);
        underTest = new TaskClientService(camundaClient, authTokenGenerator);
        serviceDetails = new ServiceDetails("jurisdiction", "caseType");
        evaluateDmnRequest = new EvaluateDmnRequest(Map.of("name",DmnValue.dmnStringValue("test")),serviceDetails);
        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
    }

    @Test
    void evaluateDmnRequest() throws JSONException {
        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            evaluateDmnRequest.getServiceDetails().getJurisdiction(),
            evaluateDmnRequest.getServiceDetails().getCaseType(),
            evaluateDmnRequest
        )).thenReturn(mockResponse());

        List<Map<String,Object>> task = underTest.evaluate(evaluateDmnRequest, "test");

        assertThat(task).isEqualTo(mockResponse());
    }

    @Test
    void evaluateForEmptyDmn() {
        List<Map<String,Object>> ts = emptyList();
        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            serviceDetails.getJurisdiction(),
            serviceDetails.getCaseType(),
            evaluateDmnRequest
        )).thenReturn(ts);

        List<Map<String,Object>> task = underTest.evaluate(evaluateDmnRequest, "test");

        assertEquals(task, new ArrayList<>());
    }
}
