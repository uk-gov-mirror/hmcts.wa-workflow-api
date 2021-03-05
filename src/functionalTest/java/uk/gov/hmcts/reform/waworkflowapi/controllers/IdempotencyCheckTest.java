package uk.gov.hmcts.reform.waworkflowapi.controllers;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyKeysRepository;
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
    private IdempotencyKeysRepository idempotencyKeysRepository;

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
        // fixme: uncomment below lines once the idempotencyTaskWorker is released
        //        assertNewIdempotentKeyIsAddedInDb(idempotentKey);
        cleanUp(taskId, serviceAuthorizationToken); //We can do the cleaning here now

        //        sendMessage(processVariables); //We send another message for the same idempotencyKey
        //        List<String> processIds = getProcessIdsForGivenIdempotentKey(idempotentKey);
        //        assertThereIsOnlyOneProcessWithDuplicateEqualToTrue(processIds);
    }

    private void assertThereIsOnlyOneProcessWithDuplicateEqualToTrue(List<String> processIds) {
        Assertions.assertThat((int) processIds.stream()
            .filter(this::getIsDuplicateVariableValue)
            .count()).isEqualTo(1);
    }

    private List<String> getProcessIdsForGivenIdempotencyKey(String idempotencyKey) {
        AtomicReference<List<String>> processIdsResponse = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/history/process-instance")
                    .param("variables", "idempotencyKey_eq_" + idempotencyKey)
                    .when()
                    .get();

                //number of messages sent, equivalent to processes created
                result.then().assertThat()
                    .statusCode(HttpStatus.OK_200)
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

    private void assertNewIdempotencyKeyIsAddedInDb(String idempotencyKey) {
        Optional<IdempotencyKeys> savedEntity = idempotencyKeysRepository.findById(new IdempotentId(idempotencyKey, "ia"));
        assertThat(savedEntity.isPresent()).isTrue();
    }

    private String assertTaskIsCreated() {
        AtomicReference<String> response = new AtomicReference<>();
        await()
            .ignoreExceptions()
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = given()
                    .header(SERVICE_AUTHORIZATION, serviceAuthorizationToken)
                    .contentType(APPLICATION_JSON_VALUE)
                    .baseUri(camundaUrl)
                    .basePath("/task")
                    .param("processVariables", "caseId_eq_" + caseId)
                    .when()
                    .get();

                result.then().assertThat()
                    .statusCode(HttpStatus.OK_200)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("[0].name", is("Provide Respondent Evidence"))
                    .body("[0].formKey", is("provideRespondentEvidence"));

                response.set(
                    result.then()
                        .extract()
                        .path("[0].id")
                );
                return true;
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
