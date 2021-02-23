package uk.gov.hmcts.reform.waworkflowapi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class IdempotencyCheckTest extends SpringBootFunctionalBaseTest {

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private String serviceAuthorizationToken;
    private String caseId;
    private String idempotentKey;
    private Map<String, DmnValue<?>> processVariables;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();
        idempotentKey = UUID.randomUUID().toString();

        serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAuthorizationHeaders()
                .getValue(SERVICE_AUTHORIZATION);


        processVariables = createProcessVariables(idempotentKey, "ia");
    }

    @Test
    public void given_two_tasks_with_the_same_idempotentKey_and_different_tenantId_should_not_be_deemed_as_duplicated() {
        sendMessage(processVariables);
        String taskId = assertTaskIsCreated(caseId);
        assertTaskHasExpectedVariableValues(taskId);
        assertNewIdempotentKeyIsAddedInDb(idempotentKey, "ia");
        cleanUp(taskId, serviceAuthorizationToken); //We do the cleaning here to avoid clashing with other tasks

        processVariables = createProcessVariables(idempotentKey, "wa");
        sendMessage(processVariables); //We send another message for the same idempotencyKey and different tenantId
        taskId = assertTaskIsCreated(caseId);
        assertTaskHasExpectedVariableValues(taskId);
        assertNewIdempotentKeyIsAddedInDb(idempotentKey, "wa");
        cleanUp(taskId, serviceAuthorizationToken); //We do the cleaning here to avoid clashing with other tasks

        List<String> processIds = getProcessIdsForGivenIdempotentKey(idempotentKey);
        assertNumberOfDuplicatedProcesses(processIds, 0);
    }

    @Test
    public void given_two_tasks_with_the_same_idempotentId_should_tag_one_as_duplicated() {
        sendMessage(processVariables);

        String taskId = assertTaskIsCreated(caseId);
        assertTaskHasExpectedVariableValues(taskId);
        assertNewIdempotentKeyIsAddedInDb(idempotentKey, "ia");

        cleanUp(taskId, serviceAuthorizationToken); //We can do the cleaning here now

        sendMessage(processVariables); //We send another message for the same idempotencyKey
        List<String> processIds = getProcessIdsForGivenIdempotentKey(idempotentKey);
        assertNumberOfDuplicatedProcesses(processIds, 1);
    }

    private void assertNumberOfDuplicatedProcesses(List<String> processIds, int expectedNumberOfDuplicatedProcesses) {
        Assertions.assertThat((int) processIds.stream()
            .filter(this::getIsDuplicateVariableValue)
            .count()).isEqualTo(expectedNumberOfDuplicatedProcesses);
    }

    private List<String> getProcessIdsForGivenIdempotentKey(String idempotentKey) {
        AtomicReference<List<String>> processIdsResponse = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(2, TimeUnit.SECONDS)
            .atMost(20, TimeUnit.SECONDS)
            .until(() -> {
                List<String> ids;
                ids = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/history/process-instance")
                    .param("variables", "idempotentKey_eq_" + idempotentKey)
                    .when()
                    .get()
                    .prettyPeek()
                    .then()
                    .extract().body().path("id");

                processIdsResponse.set(ids);

                return ids.size() == 2; //number of messages sent, equivalent to processes created
            });

        return processIdsResponse.get();
    }

    private Map<String, DmnValue<?>> createProcessVariables(String idempotentKey, String jurisdiction) {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return mockProcessVariables(
            dueDate,
            "Provide Respondent Evidence",
            "provideRespondentEvidence",
            "external",
            caseId,
            idempotentKey,
            jurisdiction
        );
    }

    private void assertNewIdempotentKeyIsAddedInDb(String idempotentKey, String jurisdiction) {
        await()
            .ignoreExceptions()
            .pollInterval(2, TimeUnit.SECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> {
                given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(testUrl)
                    .basePath("/testing/idempotentKeys/search/findByIdempotencyKeyAndTenantId")
                    .params(
                        "idempotencyKey", idempotentKey,
                        "tenantId", jurisdiction
                    )
                    .when()
                    .get()
                    .prettyPeek()
                    .then()
                    .body("idempotencyKey", is(idempotentKey))
                    .body("tenantId", is(jurisdiction));

                return true;
            });
    }

    private void assertTaskHasExpectedVariableValues(String taskId) {
        await()
            .ignoreExceptions()
            .and()
            .pollInterval(5, TimeUnit.SECONDS)
            .atMost(15, TimeUnit.SECONDS)
            .until(() -> {
                String groupId = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/task/" + taskId + "/identity-links?type=candidate")
                    .when()
                    .get()
                    .prettyPeek()
                    .then()
                    .extract()
                    .path("[0].groupId");

                return groupId.equals("external");
            });
    }

    private String assertTaskIsCreated(String caseId) {
        AtomicReference<String> response = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(5, TimeUnit.SECONDS)
            .atMost(15, TimeUnit.SECONDS)
            .until(() -> {

                String taskId = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/task")
                    .param("processVariables", "caseId_eq_" + caseId)
                    .when()
                    .get()
                    .prettyPeek()
                    .then()
                    .body("[0].name", is("Provide Respondent Evidence"))
                    .body("[0].formKey", is("provideRespondentEvidence"))
                    .extract()
                    .path("[0].id");

                response.set(taskId);

                return StringUtils.isNotBlank(taskId);
            });
        return response.get();
    }

    private void sendMessage(Map<String, DmnValue<?>> processVariables) {

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(new SendMessageRequest(
                "createTaskMessage",
                processVariables,
                null
            )).log().body()
            .baseUri(testUrl)
            .basePath("/workflow/message")
            .when()
            .post()
            .then()
            .statusCode(HttpStatus.NO_CONTENT_204);
    }

    private boolean getIsDuplicateVariableValue(String processInstanceId) {
        AtomicReference<Boolean> response = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(5, TimeUnit.SECONDS)
            .atMost(15, TimeUnit.SECONDS)
            .until(() -> {
                boolean isDuplicate = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/history/variable-instance")
                    .param("processInstanceId", processInstanceId)
                    .and().param("variableName", "isDuplicate")
                    .when()
                    .get()
                    .then()
                    .assertThat().body("[0].value", notNullValue())
                    .extract().body().path("[0].value");

                response.set(isDuplicate);

                return true;
            });
        return response.get();
    }


}
