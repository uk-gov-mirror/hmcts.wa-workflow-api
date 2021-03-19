package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
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
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createMessageTask",
                processVariables,
                null,
                false
            )).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void transition_creates_a_task_with_default_due_date() {

        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            null,
            "Process Application",
            "processApplication",
            "TCW",
            caseId,
            UUID.randomUUID().toString(), "ia"
        );

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null,
                false
            ))
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

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

                taskIdResponse.set(taskId);

                return true;
            });

        String taskId = taskIdResponse.get();
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
    public void transition_creates_a_task_with_due_date() {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            "Provide Respondent Evidence",
            "provideRespondentEvidence",
            "external",
            caseId,
            UUID.randomUUID().toString(), "ia"
        );

        Response request = given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null,
                false
            ))
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post();

        request.then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/task")
                    .param("processVariables", "caseId_eq_" + caseId)
                    .when()
                    .get();

                result.then().assertThat()
                    .body("size()", is(1))
                    .body("[0].name", is("Provide Respondent Evidence"))
                    .body("[0].formKey", is("provideRespondentEvidence"));

                taskIdResponse.set(
                    result.then()
                        .extract()
                        .path("[0].id")
                );

                return true;
            });

        String taskId = taskIdResponse.get();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {
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

                return true;
            });

        cleanUp(taskId, serviceAuthorizationToken);
    }

    @Test
    public void should_not_be_able_to_post_as_message_does_not_exist() {
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            ZonedDateTime.now().toString(),
            "Process Application", "processApplication",
            "TCW",
            caseId,
            UUID.randomUUID().toString(), "ia"
        );
        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "non-existent-message",
                processVariables,
                null,
                false
            )).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }

}
