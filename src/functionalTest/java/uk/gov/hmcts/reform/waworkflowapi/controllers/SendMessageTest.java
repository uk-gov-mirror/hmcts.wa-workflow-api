package uk.gov.hmcts.reform.waworkflowapi.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootFunctionalBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.ActivityInstance;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.entities.SpecificStandaloneRequest;
import uk.gov.hmcts.reform.waworkflowapi.services.AuthorizationHeadersProvider;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyMap;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
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
        String delayUntil = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            null,
            delayUntil,
            "Process Application",
            "processApplication",
            caseId,
            UUID.randomUUID().toString(), TENANT_ID_WA
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
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
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


                log.info("transition_creates_a_task_with_default_due_date body:{}",
                    result.then().extract().body().asString());

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
            .jurisdiction(TENANT_ID_WA)
            .caseType("WaCaseType")
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
            .jurisdiction(TENANT_ID_WA)
            .caseType("WaCaseType")
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
            .jurisdiction(TENANT_ID_WA)
            .caseType("WaCaseType")
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
        String delayUntil = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            delayUntil,
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
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + caseId)
                );

                log.info("transition_creates_a_task_with_due_date body:{}", result.then().extract().body().asString());
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
    public void transition_creates_a_task_with_due_date_for_wa() {
        String dueDate = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String delayUntil = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            dueDate,
            delayUntil,
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
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + caseId)
                );

                log.info("transition_creates_a_task_with_due_date_for_wa body:{}",
                    result.then().extract().body().asString());

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
        //cleanUpTask(taskId, REASON_COMPLETED);
    }

    @Test
    public void should_not_be_able_to_post_as_message_does_not_exist() {
        String delayUntil = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            ZonedDateTime.now().toString(),
            delayUntil,
            "Process Application", "processApplication",
            caseId,
            UUID.randomUUID().toString(),
            TENANT_ID_WA
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

    @Test
    public void should_creates_a_judicial_task_with_role_assignment_property_for_wa() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestJudiciary")
            .taskName("Review Specific Access Request")
            .roleCategory("JUDICIAL")
            .caseId(caseId)
            .additionalProperties(
                Map.of(
                    "roleAssignmentId", DmnValue.dmnStringValue("123456789")
                )
            )
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertionsForAdditionalProperties(response, request);
    }

    @Test
    public void should_creates_a_judicial_task_with_additional_properties_wa() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestLegalOps")
            .taskName("Review Specific Access Request")
            .roleCategory("JUDICIAL")
            .caseId(caseId)
            .additionalProperties(
                Map.of(
                    "roleAssignmentId", DmnValue.dmnStringValue("123456789"),
                    "key1", DmnValue.dmnStringValue("value1"),
                    "key2", DmnValue.dmnStringValue("value2"),
                    "key3", DmnValue.dmnStringValue("value3"),
                    "key4", DmnValue.dmnStringValue("value4"),
                    "key5", DmnValue.dmnStringValue("value5"),
                    "key7", DmnValue.dmnStringValue("value7")
                )
            )
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertionsForAdditionalProperties(response, request);
    }

    @Test
    public void should_creates_a_task_with_additional_properties_when_property_count_more_then_dmn_property_count() {

        SpecificStandaloneRequest request = SpecificStandaloneRequest.builder()
            .authenticationHeaders(authenticationHeaders)
            .jurisdiction(TENANT_ID_WA)
            .caseType("waCaseType")
            .taskType("reviewSpecificAccessRequestAdmin")
            .taskName("Review Specific Access Request")
            .roleCategory("JUDICIAL")
            .caseId(caseId)
            .additionalProperties(
                Map.of(
                    "roleAssignmentId", DmnValue.dmnStringValue("123456789"),
                    "key1", DmnValue.dmnStringValue("value1"),
                    "key2", DmnValue.dmnStringValue("value2"),
                    "key3", DmnValue.dmnStringValue("value3"),
                    "key4", DmnValue.dmnStringValue("value4"),
                    "key5", DmnValue.dmnStringValue("value5"),
                    "key7", DmnValue.dmnStringValue("value7")
                )
            )
            .build();

        Response response = createSpecifiedStandaloneTask(request);

        assertionsForAdditionalProperties(response, request);
    }

    @Test
    public void should_create_a_delayed_task_with_two_days_delay() {
        String delayUntil = ZonedDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Map<String, DmnValue<?>> processVariables = mockProcessVariables(
            null,
            delayUntil,
            "Process Application",
            "processApplication",
            caseId,
            UUID.randomUUID().toString(), TENANT_ID_WA
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

        AtomicReference<String> processIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                // Check process is created
                Response processResult = camundaApiActions.get(
                    "/process-instance",
                    new Headers(authenticationHeaders),
                    Map.of(
                        "variables", "caseId_eq_" + caseId
                    ));

                processResult.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("size()", is(1));


                processIdResponse.set(
                    processResult.then()
                        .extract().path("[0].id")
                );

                // Check process state. It should be at processStartTimer sate waiting for delayUntil time to lapse
                Response activityResult = camundaApiActions.get(
                    "/process-instance/{id}/activity-instances",
                    processIdResponse.get(),
                    new Headers(authenticationHeaders));

                ObjectMapper mapper = new ObjectMapper();
                List<ActivityInstance> activityInstance = mapper.convertValue(
                    activityResult.then()
                        .extract().path("childActivityInstances"),
                    new TypeReference<List<ActivityInstance>>(){});
                assertEquals("processStartTimer", activityInstance.get(0).getActivityId());
                assertEquals("intermediateTimer", activityInstance.get(0).getActivityType());

                // Make sure no task is created for the above process
                Response taskResult = camundaApiActions.get(
                    "/task",
                    new Headers(authenticationHeaders),
                    Map.of(
                        "processVariables", "caseId_eq_" + caseId
                    ));


                taskResult.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(0));

                return true;
            });

        String processId = processIdResponse.get();
        cleanUpProcess(processId);
    }

    private void assertions(Response response, SpecificStandaloneRequest specificStandaloneRequest) {
        response.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(specificStandaloneRequest.getAuthenticationHeaders()),
                    // Because the ccd case does not exist it does not configure the local variables
                    // so we will search using processVariables
                    Map.of("processVariables", "caseId_eq_" + specificStandaloneRequest.getCaseId())
                );

                log.info("assertions body:{}", result.then().extract().body().asString());

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

    private void assertionsForAdditionalProperties(Response response, SpecificStandaloneRequest specificStandaloneRequest) {
        response.then().assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        AtomicReference<String> taskIdResponse = new AtomicReference<>();
        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    "/task",
                    new Headers(specificStandaloneRequest.getAuthenticationHeaders()),
                    Map.of("processVariables", "caseId_eq_" + specificStandaloneRequest.getCaseId())
                );

                log.info("assertionsForAdditionalProperties task body:{}", result.then().extract().body().asString());

                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("size()", is(1))
                    .body("[0].name", is(specificStandaloneRequest.getTaskName()));

                taskIdResponse.set(result.then().extract().path("[0].id"));

                return true;
            });


        //Additional Property assertion
        String taskId = taskIdResponse.get();
        String processVariablesPath = String.format("/task/%s/variables/additionalProperties", taskId);

        await()
            .ignoreException(AssertionError.class)
            .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
            .atMost(FT_STANDARD_TIMEOUT_SECS, TimeUnit.SECONDS)
            .until(() -> {

                Response result = camundaApiActions.get(
                    processVariablesPath,
                    specificStandaloneRequest.getAuthenticationHeaders()
                );

                log.info("assertionsForAdditionalProperties processVariables body:{}",
                    result.then().extract().body().asString());

                result.then().assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("type", is("Object"));

                JsonPath jsonPath = result.then().extract().jsonPath();
                Map<String, Object> expectedMap = specificStandaloneRequest.getAdditionalProperties();

                AtomicReference<Map<String, String>> actualMap = new AtomicReference<>();
                AtomicReference<DmnValue> dmnValue = new AtomicReference<>();

                expectedMap.keySet().forEach((key) -> {
                    System.out.println(key);
                    actualMap.set(jsonPath.getMap(String.format("value.%s", key)));
                    dmnValue.set(new DmnValue<>(actualMap.get().get("value"), actualMap.get().get("type")));
                    assertEquals(expectedMap.get(key), dmnValue.get());
                });

                return true;
            });


        cleanUpTask(taskId, REASON_COMPLETED);
    }

}
