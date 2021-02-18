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
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotentKeysRepository;
import uk.gov.hmcts.reform.waworkflowapi.utils.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static net.serenitybdd.rest.SerenityRest.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class IdempotencyCheckTest extends SpringBootFunctionalBaseTest {

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private IdempotentKeysRepository idempotentKeysRepository;

    private String serviceAuthorizationToken;
    private String caseId;

    @Before
    public void setUp() {
        caseId = UUID.randomUUID().toString();

        serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAuthorizationHeaders()
                .getValue(SERVICE_AUTHORIZATION);
    }

    @Test
    public void transition_creates_a_task_and_goes_through_external_task() {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String idempotentKey = UUID.randomUUID().toString();
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            "Provide Respondent Evidence",
            "provideRespondentEvidence",
            "external",
            caseId,
            idempotentKey
        );

        sendMessage(processVariables);
        String taskId = assertTaskIsCreated();
        assertTaskHasExpectedVariableValues(taskId);
        // fixme: uncomment below lines once the idempotencyTaskWorker is released
        //        assertNewIdempotentKeyIsAddedInDb(idempotentKey);
        cleanUp(taskId, serviceAuthorizationToken); //We can do the cleaning here now

        sendMessage(processVariables); //We send another message for the same idempotencyKey
        List<String> processIds = getProcessIdsForGivenIdempotentKey(idempotentKey);
        assertThereIsOnlyOneProcessWithDuplicateEqualToTrue(processIds);
    }

    private void assertThereIsOnlyOneProcessWithDuplicateEqualToTrue(List<String> processIds) {
        Assertions.assertThat((int) processIds.stream()
            .filter(this::getIsDuplicateVariableValue)
            .count()).isEqualTo(1);
    }

    private List<String> getProcessIdsForGivenIdempotentKey(String idempotentKey) {
        AtomicReference<List<String>> processIdsResponse = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(2, TimeUnit.SECONDS)
            .atMost(20, TimeUnit.MINUTES)
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

    private void assertNewIdempotentKeyIsAddedInDb(String idempotentKey) {
        Optional<IdempotentKeys> savedEntity = idempotentKeysRepository.findById(new IdempotentId(idempotentKey, "ia"));
        assertThat(savedEntity.isPresent()).isTrue();
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

    private String assertTaskIsCreated() {
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

        SendMessageRequest createTaskMessage = new SendMessageRequest(
            "createTaskMessage",
            processVariables,
            null
        );

        SendMessageRequest msg = SendMessageRequest.builder()
            .businessKey("pr-138")
            .messageName("createTaskMessage")
            .processVariables(processVariables)
            .build();

        given()
            .relaxedHTTPSValidation()
            .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
            .contentType(APPLICATION_JSON_VALUE)
            .body(msg).log().body()
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
