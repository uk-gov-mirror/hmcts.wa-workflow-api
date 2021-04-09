package uk.gov.hmcts.reform.waworkflowapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.config.RestApiActions;
import uk.gov.hmcts.reform.waworkflowapi.services.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.LOWER_CAMEL_CASE;
import static com.fasterxml.jackson.databind.PropertyNamingStrategy.SNAKE_CASE;
import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class SpringBootFunctionalBaseTest {

    public static final String WA_TASK_INITIATION_IA_ASYLUM = "wa-task-initiation-ia-asylum";
    public static final String TENANT_ID = "ia";
    public static final int FT_STANDARD_TIMEOUT_SECS = 30;
    protected RestApiActions restApiActions;
    protected RestApiActions camundaApiActions;
    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Value("${targets.instance}")
    private String testUrl;

    @Value("${camunda.url}")
    private String camundaUrl;

    @Before
    public void setUpGivens() {
        restApiActions = new RestApiActions(testUrl, SNAKE_CASE).setUp();
        camundaApiActions = new RestApiActions(camundaUrl, LOWER_CAMEL_CASE).setUp();
    }

    public void cleanUp(String taskId) {
        given()
            .header(authorizationHeadersProvider.getAuthorizationHeaders())
            .contentType(APPLICATION_JSON_VALUE)
            .baseUri(camundaUrl)
            .basePath("/task/" + taskId + "/complete")
            .when()
            .post();

        await()
            .ignoreException(AssertionError.class)
            .pollInterval(2, TimeUnit.SECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> {
                String deleteReason = given()
                    .header(authorizationHeadersProvider.getAuthorizationHeaders())
                    .contentType(APPLICATION_JSON_VALUE)
                    .accept(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .when()
                    .get("/history/task?taskId=" + taskId)
                    .then()
                    .extract().path("[0].deleteReason");

                return deleteReason.equals("completed");
            });

    }

    public Map<String, DmnValue<?>> mockProcessVariables(
        String dueDate,
        String name,
        String taskId,
        String group,
        String caseId,
        String idempotencyKey,
        String jurisdiction
    ) {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        processVariables.put("dueDate", DmnValue.dmnStringValue(dueDate));
        processVariables.put("group", DmnValue.dmnStringValue(group));
        processVariables.put("name", DmnValue.dmnStringValue(name));
        processVariables.put("jurisdiction", DmnValue.dmnStringValue(jurisdiction));
        processVariables.put("caseType", DmnValue.dmnStringValue("asylum"));
        processVariables.put("taskId", DmnValue.dmnStringValue(taskId));
        processVariables.put("caseId", DmnValue.dmnStringValue(caseId));
        processVariables.put("idempotencyKey", DmnValue.dmnStringValue(idempotencyKey));

        String delayUntilTimer = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        processVariables.put("delayUntil", DmnValue.dmnStringValue(delayUntilTimer));

        return processVariables;
    }
}
