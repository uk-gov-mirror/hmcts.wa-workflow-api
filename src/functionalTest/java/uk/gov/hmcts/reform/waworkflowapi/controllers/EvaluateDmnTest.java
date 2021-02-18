package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Ignore;
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

@Ignore
public class EvaluateDmnTest extends SpringBootFunctionalBaseTest {

    @Autowired
    public AuthorizationHeadersProvider authorizationHeadersProvider;

    private String serviceAuthorizationToken;

    @Before
    public void setUp() {
        serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAuthorizationHeaders()
                .getValue(SERVICE_AUTHORIZATION);
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_403_response_code() {

        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(null))
            .baseUri(testUrl)
            .pathParam("key", WA_TASK_INITIATION_IA_ASYLUM)
            .pathParam("tenant-id",TENANT_ID)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void should_evaluate_and_return_dmn_results() {
        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(mockVariables()))
            .baseUri(testUrl)
            .pathParam("key",WA_TASK_INITIATION_IA_ASYLUM)
            .pathParam("tenant-id",TENANT_ID)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.OK_200)
            .body("results[0].size()", equalTo(4))
            .body("results[0].name.value", equalTo("Process Application"))
            .body("results[0].workingDaysAllowed.value", equalTo(2))
            .body("results[0].taskId.value", equalTo("processApplication"))
            .body("results[0].group.value", equalTo("TCW"));


    }

    @Test
    public void not_be_able_find_incorrect_dmn_table() {

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(mockVariables()))
            .baseUri(testUrl)
            .pathParam("key","non-existent")
            .pathParam("tenant-id",TENANT_ID)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR_500);

    }

    private Map<String, DmnValue<?>> mockVariables() {
        return Map.of("eventId",DmnValue.dmnStringValue("submitAppeal"),
                      "postEventState",DmnValue.dmnStringValue(""));
    }

}

