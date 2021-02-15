package uk.gov.hmcts.reform.waworkflowapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class SpringBootFunctionalBaseTest {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String WA_TASK_INITIATION_IA_ASYLUM = "wa-task-initiation-ia-asylum";
    public static final String TENANT_ID = "ia";

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

        await()
            .ignoreException(AssertionError.class)
            .pollInterval(2, TimeUnit.SECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> {
                String deleteReason = given()
                    .header(SERVICE_AUTHORIZATION, token)
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
        String idempotentKey
    ) {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        processVariables.put("dueDate", DmnValue.dmnStringValue(dueDate));
        processVariables.put("group", DmnValue.dmnStringValue(group));
        processVariables.put("name", DmnValue.dmnStringValue(name));
        processVariables.put("jurisdiction", DmnValue.dmnStringValue("ia"));
        processVariables.put("caseType", DmnValue.dmnStringValue("asylum"));
        processVariables.put("taskId", DmnValue.dmnStringValue(taskId));
        processVariables.put("caseId", DmnValue.dmnStringValue(caseId));
        processVariables.put("idempotentKey", DmnValue.dmnStringValue(idempotentKey));

        String delayUntilTimer = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        processVariables.put("delayUntil", DmnValue.dmnStringValue(delayUntilTimer));

        return processVariables;
    }
}
