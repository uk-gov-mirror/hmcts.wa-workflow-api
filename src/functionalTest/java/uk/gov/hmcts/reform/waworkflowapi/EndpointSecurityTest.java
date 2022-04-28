package uk.gov.hmcts.reform.waworkflowapi;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;

import static java.util.Collections.emptyMap;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class EndpointSecurityTest extends SpringBootFunctionalBaseTest {

    @Value("${targets.instance}")
    private String testUrl;

    @Before
    public void setUp() {
        RestAssured.baseURI = testUrl;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("Welcome");
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    @Test
    public void should_return_401_when_no_service_token_provided() {

        given()
            .relaxedHTTPSValidation()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .baseUri(testUrl)
            .when()
            .pathParam("key", WA_TASK_INITIATION_IA_ASYLUM)
            .pathParam("tenant-id", TENANT_ID_IA)
            .basePath("/workflow/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());

        given()
            .relaxedHTTPSValidation()
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                emptyMap(),
                null,
                false
            ))
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
