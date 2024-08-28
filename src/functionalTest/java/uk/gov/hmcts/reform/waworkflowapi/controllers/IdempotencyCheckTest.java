package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.services.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class IdempotencyCheckTest extends SpringBootFunctionalBaseTest {

    private static final String ENDPOINT_COMPLETE_TASK = "task/{task-id}/complete";

    private static final String ENDPOINT_HISTORY_TASK = "history/task";

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private String caseId;
    private String idempotencyKey;
    private Map<String, DmnValue<?>> processVariables;

    private Header authenticationHeaders;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();
        idempotencyKey = UUID.randomUUID().toString();
        authenticationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();

        processVariables = createProcessVariables(idempotencyKey, "wa");
    }

    @Test
    public void given_two_tasks_with_the_same_idempotentKey_and_different_tenantId_should_not_be_deemed_as_duplicated() {

        sendMessage(processVariables);
        final String taskId = assertTaskIsCreated(caseId);
        String tenantId1 = "wa";
        assertNewIdempotentKeyIsAddedToDb(idempotencyKey, tenantId1);
        cleanUpTask(taskId, REASON_COMPLETED);  //We do the cleaning here to avoid clashing with other tasks

        String tenantId2 = "ia";
        processVariables = createProcessVariables(idempotencyKey, tenantId2);

        sendMessage(processVariables); //We send another message for the same idempotencyKey and different tenantId
        final String taskId2 = assertTaskIsCreated(caseId);
        assertNewIdempotentKeyIsAddedToDb(idempotencyKey, tenantId2);
        cleanUpTask(taskId2, REASON_COMPLETED);  //We do the cleaning here to avoid clashing with other tasks

        List<String> processIds = getProcessIdsForGivenIdempotencyKey(idempotencyKey);
        assertNumberOfDuplicatedProcesses(processIds, 0);

    }

    @Test
    public void completeATask() {

        sendMessage(processVariables);
        //final String taskId = assertTaskIsCreated(caseId);
        final String taskId = "7129258e-6547-11ef-926f-424c97e6e528";
        String tenantId1 = "wa";
        assertNewIdempotentKeyIsAddedToDb(idempotencyKey, tenantId1);
        cleanUpTask1(taskId, REASON_COMPLETED);  //We do the cleaning here to avoid clashing with other tasks
    }

    @Test
    public void given_two_tasks_with_the_same_idempotentId_should_tag_one_as_duplicated() {

        sendMessage(processVariables);
        final String taskId = assertTaskIsCreated(caseId);
        assertNewIdempotentKeyIsAddedToDb(idempotencyKey, "wa");

        sendMessage(processVariables); //We send another message for the same idempotencyKey
        List<String> processIds = getProcessIdsForGivenIdempotencyKey(idempotencyKey);
        assertNumberOfDuplicatedProcesses(processIds, 1);

        cleanUpTask(taskId, REASON_COMPLETED);
    }

    private void assertNumberOfDuplicatedProcesses(List<String> processIds, int expectedNumberOfDuplicatedProcesses) {
        assertThat((int) processIds.stream()
            .filter(this::getIsDuplicateVariableValue)
            .count()).isEqualTo(expectedNumberOfDuplicatedProcesses);
    }

    private List<String> getProcessIdsForGivenIdempotencyKey(String idempotencyKey) {
        AtomicReference<List<String>> processIdsResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/history/process-instance",
                    new Headers(authenticationHeaders),
                    Map.of(
                        "variables", "idempotencyKey_eq_" + idempotencyKey
                    )
                );
                log.info("getProcessIdsForGivenIdempotencyKey-body:{}", result.then().extract().body().asString());
                result.prettyPrint();

                //number of messages sent, equivalent to processes created
                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("size()", is(2));

                processIdsResponse.set(
                    result.then()
                        .extract().body().path("id")
                );
                return true;
            });

        return processIdsResponse.get();
    }

    private Map<String, DmnValue<?>> createProcessVariables(String idempotentKey, String jurisdiction) {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String delayUntil = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return mockProcessVariables(
            dueDate,
            delayUntil,
            "Process Application",
            "processApplication",
            caseId,
            idempotentKey,
            jurisdiction
        );
    }

    private void assertNewIdempotentKeyIsAddedToDb(String idempotencyKey, String jurisdiction) {
        log.info(
            "Asserting idempotentId({}) was added to DB...",
            new IdempotentId(idempotencyKey, jurisdiction)
        );
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                log.info("assertNewIdempotentKeyIsAddedToDb idempotencyKey:{} jurisdiction:{}",
                    idempotencyKey, jurisdiction);

                String idempotencyEndPoint = String.format("/workflow/idempotency/%s/%s", idempotencyKey, jurisdiction);
                Response result = restApiActions.get(idempotencyEndPoint, authenticationHeaders);

                log.info("assertNewIdempotentKeyIsAddedToDb result body:{}", result.getBody());

                return HttpStatus.OK.value() == result.getStatusCode();
            });
        log.info("assertNewIdempotentKeyIsAddedToDb idempotentId[{}] found in DB.",
            new IdempotentId(idempotencyKey, jurisdiction));
    }

    private String assertTaskIsCreated(String caseId) {
        AtomicReference<String> taskId = new AtomicReference<>("");
        AtomicReference<Response> response = new AtomicReference<>(null);
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {
                log.info("assertTaskIsCreated");
                response.set(
                    camundaApiActions.get(
                        "/task",
                        new Headers(authenticationHeaders),
                        Map.of("processVariables", "caseId_eq_" + caseId)
                    )
                );

                log.info("assertTaskIsCreated body:{}", response.get().then().extract().body().asString());
                //number of messages sent, equivalent to processes created
                response.get().then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("[0].name", is("Process Application"));

                taskId.set(
                    response.get().then()
                        .extract()
                        .path("[0].id")
                );
                return true;
            });

        return taskId.get();
    }

    private void sendMessage(Map<String, DmnValue<?>> processVariables) {

        Response result = restApiActions.post(
            "/workflow/message",
            new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null,
                false
            ),
            authenticationHeaders
        );

        result.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    private boolean getIsDuplicateVariableValue(String processInstanceId) {
        AtomicReference<Boolean> response = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/history/variable-instance",
                    new Headers(authenticationHeaders),
                    Map.of(
                        "processInstanceId", processInstanceId,
                        "variableName", "isDuplicate"
                    )
                );

                log.info("getIsDuplicateVariableValue body:{}", result.then().extract().body().asString());
                boolean isDuplicate = result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .assertThat().body("[0].name", is("isDuplicate"))
                    .assertThat().body("[0].value", notNullValue())
                    .extract().body().path("[0].value");

                response.set(isDuplicate);

                return true;
            });
        return response.get();
    }

    private void cleanUpTask1(String taskId, String reason) {
        log.info("Cleaning task {}", taskId);
        Header authorizationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();
        final Response postComplete = camundaApiActions.post(
            ENDPOINT_COMPLETE_TASK, taskId,
            new Headers(authorizationHeaders)
        );
        log.info("postCompleteResponse {}", postComplete);

       log.info("status {}", postComplete.getStatusCode());

        log.info("postCompleteResponse PRINT!!! {}", postComplete.prettyPrint());
        Response result = camundaApiActions.get(
            ENDPOINT_HISTORY_TASK,
            new Headers(authorizationHeaders),
            Map.of("taskId", taskId)
        );

        result.prettyPrint();
        result.then().assertThat()
            .statusCode(HttpStatus.OK.value())
            .body("[0].deleteReason", is(reason));
    }

}
