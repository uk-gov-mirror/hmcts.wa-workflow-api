package uk.gov.hmcts.reform.waworkflowapi.features;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestBuilder.aCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.requestRespondentEvidenceTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.unmappedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.TransitionBuilder.aTransition;

@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateTaskTest {

    private final String testUrl = System.getenv("TEST_URL") == null ? "http://localhost:8099" :  System.getenv("TEST_URL");
    private final String camundaUrl = System.getenv("CAMUNDA_URL") == null ? "http://localhost:8080/engine-rest" :  System.getenv("CAMUNDA_URL");
    private String caseId;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();
    }

    @Test
    public void transitionCreatesATaskWithDefaultDueDate() {
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(appealSubmittedCreateTaskRequest(caseId)).log().body()
            .baseUri(testUrl)
            .basePath("/tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.CREATED_201);

        Object taskId = given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "ccdId_eq_" + caseId)
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("size()", is(1))
            .body("[0].name", is("Process Task"))
            .extract()
            .path("[0].id");

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/identity-links?type=candidate")
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("[0].groupId", is("TCW"));
    }

    @Test
    public void transitionCreatesATaskWithDueDate() {
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(requestRespondentEvidenceTaskRequest(caseId)).log().body()
            .baseUri(testUrl)
            .basePath("/tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.CREATED_201);

        Object taskId = given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "ccdId_eq_" + caseId)
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("size()", is(1))
            .body("[0].name", is("Process Task"))
            .extract()
            .path("[0].id");

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/identity-links?type=candidate")
            .when()
            .get()
            .prettyPeek()
            .then()
            .body("[0].groupId", is("external"));
    }

    @Test
    public void transitionDoesNotCreatesATask() {
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(unmappedCreateTaskRequest(caseId)).log().body()
            .baseUri(testUrl)
            .basePath("/tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);

        given()
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task")
            .param("processVariables", "ccdId_eq_" + caseId)
            .when()
            .get()
            .then()
            .body("size()", is(0));
    }

    @Test
    public void transitionCreateOverdueTask() {
        ZonedDateTime dueDate = ZonedDateTime.now();
        CreateTaskRequest createTaskRequest = aCreateTaskRequest()
            .withCaseId(caseId)
            .withTransition(
                aTransition()
                    .withPreState("appealSubmitted")
                    .withEventId("requestRespondentEvidence")
                    .withPostState("awaitingRespondentEvidence")
                    .build()
            )
            .withDueDate(dueDate)
            .build();
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(createTaskRequest).log().body()
            .baseUri(testUrl)
            .basePath("/tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.CREATED_201);

        await().ignoreException(AssertionError.class).pollInterval(1, SECONDS).atMost(60, SECONDS).until(
            () -> {
                given()
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/task")
                    .param("processVariables", "ccdId_eq_" + caseId)
                    .when()
                    .get()
                    .prettyPeek()
                    .then()
                    .body("size()", is(2))
                    .body("[0].name", is("Process Task"))
                    .body("[0].due", startsWith(dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    .body("[1].name", is("Process overdue task"));

                return true;
            }
        );
    }
}
