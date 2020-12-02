package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.util.Map;
import java.util.UUID;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

public class EvaluateDmnTest extends SpringBootFunctionalBaseTest {


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

        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(null,null)).log().body()
            .baseUri(testUrl)
            .pathParam("id","getTask_IA_Asylum")
            .basePath("/workflow/decision-definition/{id}/evaluate")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.FORBIDDEN_403);
    }

    @Test
    public void transition_creates_atask_with_default_due_date() {

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new EvaluateDmnRequest(mockVariables(),serviceDetails())).log().body()
            .baseUri(testUrl)
            .pathParam("id","getTask_IA_Asylum")
            .basePath("/workflow/decision-definition/{id}/evaluate")
            .when()
            .post()
            .prettyPeek()
            .then()
            .statusCode(HttpStatus.OK_200)
            .body("results[0].size()", equalTo(4))
            .body("results[0].name.value", equalTo("Process Application"))
            .body("results[0].workingDaysAllowed.value", equalTo(2))
            .body("results[0].taskId.value", equalTo("processApplication"))
            .body("results[0].group.value", equalTo("TCW"));


    }

    private Map<String, DmnValue> mockVariables() {
        return Map.of("eventId",DmnValue.dmnStringValue("submitAppeal"),
                      "postEventState",DmnValue.dmnStringValue(""));
    }

    private ServiceDetails serviceDetails() {
        return new ServiceDetails("IA","Asylum");
    }
}

