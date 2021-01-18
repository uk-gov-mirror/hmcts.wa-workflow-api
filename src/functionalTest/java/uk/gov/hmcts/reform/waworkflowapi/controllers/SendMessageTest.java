package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SendMessageTest extends SpringBootFunctionalBaseTest {

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private String serviceAuthorizationToken;
    private String caseId;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();

        serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAuthorizationHeaders()
                .getValue(SERVICE_AUTHORIZATION);
    }

    @Test
    public void transition_creates_a_task_with_default_due_date() {

        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            null,
            "Process Application",
            "processApplication",
            "TCW"
        );

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null
            )).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        String taskId = given()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "caseId_eq_" + caseId)
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("size()", is(1))
            .body("[0].name", is("Process Application"))
            .body("[0].formKey", is("processApplication"))
            .extract()
            .path("[0].id");

        given()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/identity-links?type=candidate")
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("[0].groupId", is("TCW"));

        cleanUp(taskId, serviceAuthorizationToken);
    }

    @Test
    public void transition_creates_a_task_with_due_date() throws InterruptedException {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            "Provide Respondent Evidence",
            "provideRespondentEvidence",
            "external"
        );

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null
            )).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        String taskId = given()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "caseId_eq_" + caseId)
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("size()", is(1))
            .body("[0].name", is("Provide Respondent Evidence"))
            .body("[0].formKey", is("provideRespondentEvidence"))
            .extract()
            .path("[0].id");

        given()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/identity-links?type=candidate")
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("[0].groupId", is("external"));

        cleanUp(taskId, serviceAuthorizationToken);
    }

    private Map<String, DmnValue<?>> mockProcessVariables(
        String dueDate,
        String name,
        String taskId,
        String group
    ) {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        processVariables.put("dueDate", DmnValue.dmnStringValue(dueDate));
        processVariables.put("group", DmnValue.dmnStringValue(group));
        processVariables.put("name", DmnValue.dmnStringValue(name));
        processVariables.put("jurisdiction", DmnValue.dmnStringValue("ia"));
        processVariables.put("caseType", DmnValue.dmnStringValue("asylum"));
        processVariables.put("taskId", DmnValue.dmnStringValue(taskId));
        processVariables.put("caseId", DmnValue.dmnStringValue(caseId));

        String delayUntilTimer = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        processVariables.put("delayUntil", DmnValue.dmnStringValue(delayUntilTimer));

        return processVariables;
    }
}
