package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

public class EvaluateDmnTest extends SpringBootFunctionalBaseTest {

    private static final String ENDPOINT_BEING_TESTED =
        "/workflow/decision-definition/key/%s/tenant-id/%s/evaluate";
    private Header authenticationHeaders;

    @Before
    public void setUp() {
        authenticationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_401_response_code() {

        Response result = restApiActions.post(
            format(ENDPOINT_BEING_TESTED, WA_TASK_INITIATION_IA_ASYLUM, TENANT_ID),
            null,
            null,
            new Headers(new Header(SERVICE_AUTHORIZATION, "invalidtoken"))
        );

        result.then().assertThat()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void should_evaluate_and_return_dmn_results() {

        EvaluateDmnRequest body = new EvaluateDmnRequest(
            Map.of(
                "eventId", DmnValue.dmnStringValue("uploadHomeOfficeBundle"),
                "postEventState", DmnValue.dmnStringValue("awaitingRespondentEvidence")
            ));

        Response result = restApiActions.post(
            format(ENDPOINT_BEING_TESTED, WA_TASK_INITIATION_IA_ASYLUM, TENANT_ID),
            null,
            body,
            authenticationHeaders
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .and()
            .body("size()", equalTo(1))
            .body("results[0].name.value", equalTo("Review Respondent Evidence"))
            .body("results[0].workingDaysAllowed.value", equalTo(2))
            .body("results[0].taskId.value", equalTo("reviewRespondentEvidence"))
            .body("results[0].group.value", equalTo("TCW"))
            .body("results[0].processCategories.value", equalTo("caseProgression"));
        
    }

    @Test
    public void should_evaluate_json_data_and_return_dmn_results() {

        Map<String, Object> appealMap = new HashMap<>();
        appealMap.put("appealType", "protection");
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("Data", appealMap);

        EvaluateDmnRequest body = new EvaluateDmnRequest(
            Map.of(
                "eventId", DmnValue.dmnStringValue("submitAppeal"),
                "postEventState", DmnValue.dmnStringValue("appealSubmitted"),
                "additionalData", DmnValue.dmnMapValue(dataMap)
            ));

        Response result = restApiActions.post(
            format(ENDPOINT_BEING_TESTED, WA_TASK_INITIATION_IA_ASYLUM, TENANT_ID),
            null,
            body,
            authenticationHeaders
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("size()", equalTo(1))
            .body("results[0].name.value", equalTo("Review the appeal"))
            .body("results[0].workingDaysAllowed.value", equalTo(2))
            .body("results[0].taskId.value", equalTo("reviewTheAppeal"))
            .body("results[0].group.value", equalTo("TCW"))
            .body("results[0].processCategories.value", equalTo("caseProgression"));

    }

    @Test
    public void should_throw_an_error_when_dmn_table_does_not_exist() {

        EvaluateDmnRequest body = new EvaluateDmnRequest(
            Map.of(
                "eventId", DmnValue.dmnStringValue("submitAppeal"),
                "postEventState", DmnValue.dmnStringValue("appealSubmitted")
            ));

        Response result = restApiActions.post(
            format(ENDPOINT_BEING_TESTED, "non-existent", TENANT_ID),
            body,
            authenticationHeaders
        );

        result.then().assertThat()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

    }

}
