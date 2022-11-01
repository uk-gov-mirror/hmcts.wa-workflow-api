package uk.gov.hmcts.reform.waworkflowapi;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.config.RestApiActions;
import uk.gov.hmcts.reform.waworkflowapi.entities.SpecificStandaloneRequest;
import uk.gov.hmcts.reform.waworkflowapi.services.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.LOWER_CAMEL_CASE;
import static org.hamcrest.CoreMatchers.is;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public abstract class SpringBootFunctionalBaseTest {
    public static final String WA_TASK_INITIATION_WA_ASYLUM = "wa-task-initiation-wa-wacasetype";
    public static final String TENANT_ID_WA = "wa";
    public static final int FT_STANDARD_TIMEOUT_SECS = 60;

    public static final String REASON_COMPLETED = "completed";

    private static final String ENDPOINT_COMPLETE_TASK = "task/{task-id}/complete";
    private static final String ENDPOINT_HISTORY_TASK = "history/task";
    protected RestApiActions restApiActions;
    protected RestApiActions camundaApiActions;
    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Value("${targets.instance}")
    private String testUrl;

    @Value("${targets.camunda}")
    private String camundaUrl;

    @Before
    public void setUpGivens() {
        restApiActions = new RestApiActions(testUrl, LOWER_CAMEL_CASE).setUp();
        //Convention should be snake case will be fixed in another PR
        //restApiActions = new RestApiActions(testUrl, SNAKE_CASE).setUp();
        camundaApiActions = new RestApiActions(camundaUrl, LOWER_CAMEL_CASE).setUp();
    }

    public void cleanUpTask(String taskId, String reason) {
        log.info("Cleaning task {}", taskId);
        Header authorizationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();
        camundaApiActions.post(
            ENDPOINT_COMPLETE_TASK, taskId,
            new Headers(authorizationHeaders)
        );

        Response result = camundaApiActions.get(
            ENDPOINT_HISTORY_TASK,
            new Headers(authorizationHeaders),
            Map.of("taskId", taskId)
        );

        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("[0].deleteReason", is(reason));
    }

    public Map<String, DmnValue<?>> mockProcessVariables(
        String dueDate,
        String name,
        String taskId,
        String caseId,
        String idempotencyKey,
        String jurisdiction
    ) {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        processVariables.put("dueDate", DmnValue.dmnStringValue(dueDate));
        processVariables.put("name", DmnValue.dmnStringValue(name));
        processVariables.put("jurisdiction", DmnValue.dmnStringValue(jurisdiction));
        processVariables.put("caseType", DmnValue.dmnStringValue("WaCaseType"));
        processVariables.put("taskId", DmnValue.dmnStringValue(taskId));
        processVariables.put("caseId", DmnValue.dmnStringValue(caseId));
        processVariables.put("idempotencyKey", DmnValue.dmnStringValue(idempotencyKey));

        String delayUntilTimer = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        processVariables.put("delayUntil", DmnValue.dmnStringValue(delayUntilTimer));

        return processVariables;
    }

    public Map<String, DmnValue<?>> standaloneMockProcessVariables(
        String dueDate,
        String name,
        String taskId,
        String caseId,
        String caseType,
        String idempotencyKey,
        String jurisdiction,
        String roleCategory,
        Map<String, Object> additionalProperties

    ) {
        Map<String, DmnValue<?>> processVariables = new HashMap<>();
        processVariables.put("dueDate", DmnValue.dmnStringValue(dueDate));
        processVariables.put("name", DmnValue.dmnStringValue(name));
        processVariables.put("jurisdiction", DmnValue.dmnStringValue(jurisdiction));
        processVariables.put("caseType", DmnValue.dmnStringValue(caseType));
        processVariables.put("taskId", DmnValue.dmnStringValue(taskId));
        processVariables.put("caseId", DmnValue.dmnStringValue(caseId));
        processVariables.put("idempotencyKey", DmnValue.dmnStringValue(idempotencyKey));

        String delayUntilTimer = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        processVariables.put("delayUntil", DmnValue.dmnStringValue(delayUntilTimer));
        processVariables.put("roleCategory", DmnValue.dmnStringValue(roleCategory));
        processVariables.put("additionalProperties", DmnValue.dmnMapValue(additionalProperties));

        return processVariables;
    }

    public Response createSpecifiedStandaloneTask(SpecificStandaloneRequest specificStandaloneRequest) {

        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = standaloneMockProcessVariables(
            dueDate,
            specificStandaloneRequest.getTaskName(),
            specificStandaloneRequest.getTaskType(),
            specificStandaloneRequest.getCaseId(),
            specificStandaloneRequest.getCaseType(),
            UUID.randomUUID().toString(),
            specificStandaloneRequest.getJurisdiction(),
            specificStandaloneRequest.getRoleCategory(),
            specificStandaloneRequest.getAdditionalProperties()
        );

        SendMessageRequest body = new SendMessageRequest(
            "createTaskMessage",
            processVariables,
            null,
            false
        );

        return restApiActions.post(
            "/workflow/message",
            body,
            specificStandaloneRequest.getAuthenticationHeaders()
        );

    }
}
