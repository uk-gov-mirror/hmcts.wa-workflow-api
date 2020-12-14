package uk.gov.hmcts.reform.waworkflowapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class SpringBootFunctionalBaseTest {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String WA_TASK_INITIATION_IA_ASYLUM = "wa-task-initiation-ia-asylum";

    @Value("${targets.instance}")
    public String testUrl;

    @Value("${camunda.url}")
    public String camundaUrl;

    public void cleanUp(String taskId, String token) {
        given()
            .header(SERVICE_AUTHORIZATION, token)
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/complete")
            .when()
            .post();

        given()
            .header(SERVICE_AUTHORIZATION, token)
            .contentType(APPLICATION_JSON_VALUE)
            .accept(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .when()
            .get("/history/task?taskId=" + taskId)
            .then()
            .body("[0].deleteReason", is("completed"));
    }
}
