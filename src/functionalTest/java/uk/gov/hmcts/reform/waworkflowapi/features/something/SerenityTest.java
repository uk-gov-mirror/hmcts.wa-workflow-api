package uk.gov.hmcts.reform.waworkflowapi.features.something;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;

import static net.serenitybdd.rest.SerenityRest.given;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.unmappedCreateTaskRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class SerenityTest {

    private final String testUrl = System.getenv("TEST_URL") == null ? "http://localhost:8099" :  System.getenv("TEST_URL");

    @Test
    public void transitionCreatesATask() {
        given()
            .relaxedHTTPSValidation()
            .contentType("application/json")
            .body(appealSubmittedCreateTaskRequest()).log().body()
            .baseUri(testUrl)
            .basePath("tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.CREATED_201);
    }

    @Test
    public void transitionDoesNotCreatesATask() {
        given()
            .relaxedHTTPSValidation()
            .contentType("application/json")
            .body(unmappedCreateTaskRequest()).log().body()
            .baseUri(testUrl)
            .basePath("tasks")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);
    }
}
