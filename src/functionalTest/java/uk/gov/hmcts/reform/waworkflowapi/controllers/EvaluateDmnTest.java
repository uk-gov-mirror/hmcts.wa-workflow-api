package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.util.Map;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class EvaluateDmnTest extends SpringBootFunctionalBaseTest {

    @Autowired
    public AuthorizationHeadersProvider authorizationHeadersProvider;


    @Test
    public void should_evaluate_and_return_dmn_results() {

        Headers authenticationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();

        Response result = given()
            .relaxedHTTPSValidation()
            .headers(authenticationHeaders)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(mockVariables()))
            .baseUri(testUrl)
            .pathParam("key", WA_TASK_INITIATION_IA_ASYLUM)
            .pathParam("tenant-id", TENANT_ID)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post();


        result.then().assertThat()
            .body("size()", equalTo(1))
            .body("results[0].name.value", equalTo("Review the appeal"))
            .body("results[0].workingDaysAllowed.value", equalTo(2))
            .body("results[0].taskId.value", equalTo("reviewTheAppeal"))
            .body("results[0].group.value", equalTo("TCW"))
            .body("results[0].taskCategory.value", equalTo("Case progression"));


    }

    @Test
    public void not_be_able_find_incorrect_dmn_table() {

        Headers authenticationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();

        Response result = given()
            .relaxedHTTPSValidation()
            .headers(authenticationHeaders)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(mockVariables()))
            .baseUri(testUrl)
            .pathParam("key", "non-existent")
            .pathParam("tenant-id", TENANT_ID)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post();

        result.then().assertThat()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);

    }

    private Map<String, DmnValue<?>> mockVariables() {
        return Map.of("eventId", DmnValue.dmnStringValue("submitAppeal"),
            "postEventState", DmnValue.dmnStringValue("appealSubmitted"));
    }

}

