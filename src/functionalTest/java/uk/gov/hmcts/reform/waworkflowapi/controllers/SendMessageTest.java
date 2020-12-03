package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

public class SendMessageTest extends SpringBootFunctionalBaseTest {


    @Value("${targets.instance}")
    private String testUrl;

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private String caseId;

    private String serviceAuthorizationToken;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();
        serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAuthorizationHeaders()
                .getValue(SERVICE_AUTHORIZATION);
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {
        Map<String, DmnValue> processVariables = new HashMap<>();
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest("createMessageTask", processVariables)).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void should_initiate_camunda_send_message() {
        Map<String, DmnValue> processVariables = mockProcessVariables();
        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest("createTaskMessage", processVariables)).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);
    }

    @Test
    public void should_not_be_able_to_post_as_message_does_not_exist() {
        Map<String, DmnValue> processVariables = mockProcessVariables();
        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest("non-existent-message", processVariables)).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

    private Map<String, DmnValue> mockProcessVariables() {
        Map<String, DmnValue> processVariables = new HashMap<>();
        processVariables.put("dueDate",DmnValue.dmnStringValue("2020-09-05T14:47:01.250542+01:00"));
        processVariables.put("group",DmnValue.dmnStringValue("group"));
        processVariables.put("name",DmnValue.dmnStringValue("name"));
        processVariables.put("jurisdiction",DmnValue.dmnStringValue("IA"));
        processVariables.put("caseType",DmnValue.dmnStringValue("Asylum"));
        processVariables.put("taskId",DmnValue.dmnStringValue("provideRespondentEvidence"));

        return processVariables;
    }
}
