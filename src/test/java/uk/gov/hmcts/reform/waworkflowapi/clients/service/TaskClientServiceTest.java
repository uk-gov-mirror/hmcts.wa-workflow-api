package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskClientServiceTest {

    private static final String DELAY_UNTIL = "{\"delayUntil\":\"2022-10-13T21:00\",\"delayUntilTime\":\"18:00\","
        + "\"delayUntilOrigin\":null,\"delayUntilIntervalDays\":null,\"delayUntilNonWorkingCalendar\":null,"
        + "\"delayUntilNonWorkingDaysOfWeek\":null,\"delayUntilSkipNonWorkingDays\":null,"
        + "\"delayUntilMustBeWorkingDay\":null}";

    private CamundaClient camundaClient;
    private TaskClientService underTest;
    private AuthTokenGenerator authTokenGenerator;
    private EvaluateDmnRequest evaluateDmnRequest;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    private List<Map<String, DmnValue<?>>> mockResponse() {
        return List.of(Map.of("test", DmnValue.dmnStringValue("TestValue")));
    }

    private List<Map<String, DmnValue<?>>> mockResponseWithSpaces() {
        return List.of(Map.of(
            "key1", DmnValue.dmnStringValue("value1, value2"),
            "key2", DmnValue.dmnStringValue("value3"),
            "key3", DmnValue.dmnStringValue("value4,value5"),
            "key4", DmnValue.dmnIntegerValue(4),
            "key5", DmnValue.dmnMapValue(Map.of("key", "value")),
            "delayUntil", new DmnValue<>(DELAY_UNTIL, null)
        ));
    }

    @BeforeEach
    void setUp() {
        camundaClient = mock(CamundaClient.class);
        authTokenGenerator = mock(AuthTokenGenerator.class);
        underTest = new TaskClientService(camundaClient, authTokenGenerator);
        evaluateDmnRequest = new EvaluateDmnRequest(Map.of("name", DmnValue.dmnStringValue("test")));
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

        List<Map<String, DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");
        assertEquals(task.get(0).get("test").getValue(), "TestValue");
    }

    @Test
    void evaluateForEmptyDmn() {
        List<Map<String, DmnValue<?>>> ts = emptyList();
        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            "key",
            "id",
            evaluateDmnRequest
        )).thenReturn(ts);

        List<Map<String, DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");

        assertEquals(task, new ArrayList<>());
    }

    @Test
    void evaluateForEmptyMap() {
        Map<String, DmnValue<?>> emptyMap = new HashMap<>();
        List<Map<String, DmnValue<?>>> dmnResponse = new ArrayList<>();
        dmnResponse.add(emptyMap);
        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            "key",
            "id",
            evaluateDmnRequest
        )).thenReturn(dmnResponse);

        List<Map<String, DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");

        assertEquals(task, new ArrayList<>());
    }

    @Test
    void evaluateDmnRequestWithSpaces() {

        when(camundaClient.evaluateDmn(
            eq(BEARER_SERVICE_TOKEN),
            anyString(),
            anyString(),
            eq(evaluateDmnRequest)
        )).thenReturn(mockResponseWithSpaces());


        List<Map<String, DmnValue<?>>> task = underTest.evaluate(evaluateDmnRequest, "test", "id");
        assertEquals(task.get(0).get("key1").getValue(), "value1,value2");
        assertEquals(task.get(0).get("key2").getValue(), "value3");
        assertEquals(task.get(0).get("key3").getValue(), "value4,value5");
        assertEquals(task.get(0).get("key4").getValue(), 4);
        assertEquals(task.get(0).get("key5").getValue(), Map.of("key", "value"));
        assertEquals(task.get(0).get("delayUntil").getValue(), DELAY_UNTIL);
    }
}
