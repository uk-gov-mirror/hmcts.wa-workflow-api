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
import uk.gov.hmcts.reform.waworkflowapi.entities.SpecificStandaloneRequest;
import uk.gov.hmcts.reform.waworkflowapi.services.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyMap;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@Slf4j
public class SendMessageTest extends SpringBootFunctionalBaseTest {

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    private String caseId;

    private Header authenticationHeaders;

    @Before
    public void setUp() {
        authenticationHeaders = authorizationHeadersProvider.getAuthorizationHeaders();
        caseId = UUID.randomUUID().toString();
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_401_response_code() {

        SendMessageRequest body = new SendMessageRequest(
            "createTaskMessage",
            emptyMap(),
            null,
            false
        );

        Response response = restApiActions.post(
            "/workflow/message",
            body,
            new Headers(new Header(SERVICE_AUTHORIZATION, "invalidtoken"))
        );

        response
            .then().assertThat()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    public void transition_creates_a_task_with_default_due_date() {

        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            null,
            "Process Application",
            "processApplication",
            caseId,
            UUID.randomUUID().toString(), TENANT_ID_IA
        );

        SendMessageRequest body = new SendMessageRequest(
            "createTaskMessage",
            processVariables,
            null,
            false
        );

        Response response = restApiActions.post(
            "/workflow/message",
            body,
            authenticationHeaders
        );

        response
            .then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of(
                        "processVariables", "caseId_eq_" + caseId
                    ));


                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1))
                    .body("[0].name", is("Process Application"));

                taskIdResponse.set(
                    result.then()
                        .extract().path("[0].id")
                );

                return true;
            });

        String taskId = taskIdResponse.get();
        cleanUpTask(taskId, REASON_COMPLETED);
    }

    @Test
    public void should_creates_a_judicial_standalone_task_with_default_due_date() {
        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_IA)
            .caseType("asylum")
            .taskType("reviewSpecificAccessRequestJudiciary")
            .taskName("Review Specific Access Request")
            .roleCategory("JUDICIAL")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void should_creates_a_legal_ops_standalone_task_with_default_due_date() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_IA)
            .caseType("asylum")
            .taskType("reviewSpecificAccessRequestLegalOps")
            .taskName("Review Specific Access Request")
            .roleCategory("LEGAL_OPERATIONS")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void should_creates_a_admin_standalone_task_with_default_due_date() {


        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_IA)
            .caseType("asylum")
            .taskType("reviewSpecificAccessRequestAdmin")
            .taskName("Review Specific Access Request")
            .roleCategory("ADMINISTRATOR")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void should_creates_a_judicial_standalone_task_with_default_due_date_for_wa() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestJudiciary")
            .taskName("Review Specific Access Request")
            .roleCategory("JUDICIAL")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void should_creates_a_legal_ops_standalone_task_with_default_due_date_for_wa() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestLegalOps")
            .taskName("Review Specific Access Request")
            .roleCategory("LEGAL_OPERATIONS")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void should_creates_a_admin_standalone_task_with_default_due_date_for_wa() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestAdmin")
            .taskName("Review Specific Access Request")
            .roleCategory("ADMINISTRATOR")
            .caseId(caseId)
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertions(response, request);
    }

    @Test
    public void transition_creates_a_task_with_due_date() {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            "Review the appeal",
            "reviewTheAppeal",
            caseId,
            UUID.randomUUID().toString(),
            TENANT_ID_IA
        );

        SendMessageRequest body = new SendMessageRequest(
            "createTaskMessage",
            processVariables,
            null,
            false
        );

        Response response = restApiActions.post(
            "/workflow/message",
            body,
            authenticationHeaders
        );

        response.then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + caseId)
                );

                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("size()", is(1))
                    .body("[0].name", is("Review the appeal"));

                taskIdResponse.set(
                    result.then()
                        .extract()
                        .path("[0].id")
                );

                return true;
            });

        String taskId = taskIdResponse.get();
        cleanUpTask(taskId, REASON_COMPLETED);
    }

    @Test
    public void transition_creates_a_task_with_due_date_for_wa() {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            "Process Application",
            "processApplication",
            caseId,
            UUID.randomUUID().toString(),
            TENANT_ID_WA
        );

        SendMessageRequest body = new SendMessageRequest(
            "createTaskMessage",
            processVariables,
            null,
            false
        );

        Response response = restApiActions.post(
            "/workflow/message",
            body,
            authenticationHeaders
        );

        response.then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + caseId)
                );

                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("size()", is(1))
                    .body("[0].name", is("Process Application"));

                taskIdResponse.set(
                    result.then()
                        .extract()
                        .path("[0].id")
                );

                return true;
            });

        String taskId = taskIdResponse.get();
        cleanUpTask(taskId, REASON_COMPLETED);
    }

    @Test
    public void should_not_be_able_to_post_as_message_does_not_exist() {
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            ZonedDateTime.now().toString(),
            "Process Application", "processApplication",
            caseId,
            UUID.randomUUID().toString(),
            TENANT_ID_IA
        );
        SendMessageRequest body = new SendMessageRequest(
            "invalidMessageName",
            processVariables,
            null,
            false
        );

        Response response = restApiActions.post(
            "/workflow/message",
            body,
            authenticationHeaders
        );

        response.then()
            .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

    }

    private void assertions(Response response, SpecificStandaloneRequest specificStandaloneRequest) {
        response.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(specificStandaloneRequest.getAuthenticationHeaders()),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + specificStandaloneRequest.getCaseId())
                );

                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1))
                    .body("[0].name", is(specificStandaloneRequest.getTaskName()));

                taskIdResponse.set(result.then().extract().path("[0].id"));

                return true;
            });

        String taskId = taskIdResponse.get();
        cleanUpTask(taskId, REASON_COMPLETED);
    }

}
