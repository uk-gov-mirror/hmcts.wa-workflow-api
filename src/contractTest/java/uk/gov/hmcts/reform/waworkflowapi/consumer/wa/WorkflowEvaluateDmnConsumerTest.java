package uk.gov.hmcts.reform.waworkflowapi.consumer.wa;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnResponse;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue.dmnStringValue;

@PactTestFor(providerName = "wa_workflow_api_evaluate_dmn", port = "8899")
public class WorkflowEvaluateDmnConsumerTest extends SpringBootContractBaseTest {

    private static final String WA_EVALUATE_DMN_URL = "/workflow/decision-definition/key/someKey/tenant-id/someTenant/evaluate";

    @Test
    @PactTestFor(pactMethod = "evaluateDmn200")
    void evaluateDmn200Test(MockServer mockServer) throws JsonProcessingException {
        String actualResponseBody = SerenityRest
            .given()
            .headers(getHttpHeaders())
            .contentType(ContentType.JSON)
            .body(createMessage())
            .post(mockServer.getUrl() + WA_EVALUATE_DMN_URL)
            .then()
            .statusCode(200)
            .extract().asString();

        ObjectMapper mapper = new ObjectMapper();
        EvaluateDmnResponse readValue = mapper.readValue(actualResponseBody, EvaluateDmnResponse.class);
        Assertions.assertThat(readValue).isNotNull();
        Assertions.assertThat(readValue.getResults().get(0).get("name").getValue()).isEqualTo("some name");
        Assertions.assertThat(readValue.getResults().get(0).get("jurisdiction").getValue()).isEqualTo("WA");
        Assertions.assertThat(readValue.getResults().get(0).get("caseType").getValue()).isEqualTo("WaCaseType");
        Assertions.assertThat(readValue.getResults().get(0).get("taskId").getValue()).isEqualTo("some taskId");
        Assertions.assertThat(readValue.getResults().get(0).get("caseId").getValue()).isEqualTo("some caseId");

    }

    @Pact(provider = "wa_workflow_api_evaluate_dmn", consumer = "wa_workflow_api")
    public RequestResponsePact evaluateDmn200(PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given("evaluate dmn")
            .uponReceiving("response to return")
            .path(WA_EVALUATE_DMN_URL)
            .method(HttpMethod.POST.toString())
            .body(createMessage(), String.valueOf(ContentType.JSON))
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createResponse())
            .toPact();
    }


    private String createMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        EvaluateDmnRequest evaluateDmnRequest = new EvaluateDmnRequest(
            Map.of(
                "name", dmnStringValue("some name"),
                "jurisdiction", dmnStringValue("WA"),
                "caseType", dmnStringValue("WaCaseType"),
                "taskId", dmnStringValue("some taskId"),
                "caseId", dmnStringValue("some caseId")
            )
        );
        return objectMapper.writeValueAsString(evaluateDmnRequest);
    }

    private String createResponse() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, DmnValue<?>> map = Map.of(
            "name", dmnStringValue("some name"),
            "jurisdiction", dmnStringValue("WA"),
            "caseType", dmnStringValue("WaCaseType"),
            "taskId", dmnStringValue("some taskId"),
            "caseId", dmnStringValue("some caseId")
        );
        EvaluateDmnResponse evaluateDmnResponse = new EvaluateDmnResponse(
            Collections.singletonList(map)
        );
        return objectMapper.writeValueAsString(evaluateDmnResponse);
    }
}

