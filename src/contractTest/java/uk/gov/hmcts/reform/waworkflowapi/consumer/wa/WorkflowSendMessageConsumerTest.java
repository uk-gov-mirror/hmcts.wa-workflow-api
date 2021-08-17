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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;

import java.util.Map;

import static uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue.dmnStringValue;

@PactTestFor(providerName = "wa_workflow_api_send_message", port = "8899")
public class WorkflowSendMessageConsumerTest extends SpringBootContractBaseTest {

    private static final String WA_POST_MESSAGE_URL = "/workflow/message";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @PactTestFor(pactMethod = "executeSendMessage204")
    void sendMessage204Test(MockServer mockServer)  {
        SerenityRest
            .given()
            .headers(getHttpHeaders())
            .contentType(ContentType.JSON)
            .body(createMessage())
            .post(mockServer.getUrl() + WA_POST_MESSAGE_URL)
            .then()
            .statusCode(204);
    }

    @Pact(provider = "wa_workflow_api_send_message", consumer = "wa_workflow_api")
    public RequestResponsePact executeSendMessage204(PactDslWithProvider builder) {

        return builder
            .given("send a message to Camunda")
            .uponReceiving("message to post")
            .path(WA_POST_MESSAGE_URL)
            .method(HttpMethod.POST.toString())
            .body(createMessage(), String.valueOf(ContentType.JSON))
            .matchHeader(SERVICE_AUTHORIZATION, SERVICE_AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.NO_CONTENT.value())
            .toPact();
    }


    private String createMessage() {
        SendMessageRequest sendMessageRequest = new SendMessageRequest(
            "some other message",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "caseId", dmnStringValue("some caseId")
            ),
            null,
            false
        );
        try {
            return objectMapper.writeValueAsString(sendMessageRequest);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonProcessingException.printStackTrace();
        }
        return "";
    }
}

