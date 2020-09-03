package uk.gov.hmcts.reform.waworkflowapi.features.something;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.unmappedCreateTaskRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CreateTaskTest {

    private final String testUrl = System.getenv("TEST_URL") == null ? "http://localhost:8099" :  System.getenv("TEST_URL");
    private final String camundaUrl = System.getenv("CAMUNDA_URL") == null ? "http://localhost:8080/engine-rest" :  System.getenv("CAMUNDA_URL");
    private String caseId;

    @Before
    public void setUp() throws Exception {
        caseId = UUID.randomUUID().toString();
    }

    @Test
    public void transitionCreatesATask() {
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(appealSubmittedCreateTaskRequest(caseId)).log().body()
            .baseUri(testUrl)
            .basePath("tasks")
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
    public void transitionDoesNotCreatesATask() {
        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(unmappedCreateTaskRequest(caseId)).log().body()
            .baseUri(testUrl)
            .basePath("tasks")
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
}
