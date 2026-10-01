package uk.gov.hmcts.reform.waworkflowapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Jackson round trips for Boot 4 / Jackson 3 data-integrity risk.
 *
 * <p>Each nested class uses the same mapper generation and naming strategy as the
 * production path under test. HTTP 200 alone is not enough — these assert property
 * names and values. Functional/AAT suites remain the environment-level gate.
 */
class JacksonRoundTripTest {

    private JsonMapper camundaMapper;
    private JsonMapper taskManagementMapper;
    private JsonMapper mvcMapper;

    @BeforeEach
    void setUp() {
        camundaMapper = new CamundaFeignConfiguration().camundaJsonMapper();
        taskManagementMapper = new FeignClientSnakeCaseConfiguration().snakeCaseJsonMapper();
        // Mimic Boot's Jackson 3 MVC mapper (JavaTime built into databind 3).
        mvcMapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Nested
    @DisplayName("Camunda Feign (Jackson 3 lowerCamelCase)")
    class CamundaFeignPath {

        @Test
        void sendMessageRequest_roundTripsFixture() throws Exception {
            String fixture = readFixture("jackson-roundtrip/camunda-send-message.json");

            SendMessageRequest decoded = camundaMapper.readValue(fixture, SendMessageRequest.class);
            assertEquals("createTaskMessage", decoded.getMessageName());
            assertEquals("1234567890123456", decoded.getProcessVariables().get("caseId").getValue());
            assertEquals("String", decoded.getProcessVariables().get("caseId").getType());
            assertFalse(decoded.isAll());

            String encoded = camundaMapper.writeValueAsString(decoded);
            assertContainsField(encoded, "messageName");
            assertContainsField(encoded, "processVariables");
            assertDoesNotContainField(encoded, "message_name");
            assertDoesNotContainField(encoded, "process_variables");

            SendMessageRequest roundTripped = camundaMapper.readValue(encoded, SendMessageRequest.class);
            assertEquals(decoded, roundTripped);
        }

        @Test
        void evaluateDmnRequest_roundTripsFixture() throws Exception {
            String fixture = readFixture("jackson-roundtrip/camunda-evaluate-dmn.json");

            EvaluateDmnRequest decoded = camundaMapper.readValue(fixture, EvaluateDmnRequest.class);
            assertEquals("WA", decoded.getVariables().get("jurisdiction").getValue());
            assertEquals("WaCaseType", decoded.getVariables().get("caseType").getValue());

            String encoded = camundaMapper.writeValueAsString(decoded);
            assertContainsField(encoded, "variables");
            assertContainsField(encoded, "caseType");
            assertDoesNotContainField(encoded, "case_type");

            EvaluateDmnRequest roundTripped = camundaMapper.readValue(encoded, EvaluateDmnRequest.class);
            assertEquals(decoded.getVariables().get("jurisdiction"),
                roundTripped.getVariables().get("jurisdiction"));
            assertEquals(decoded.getVariables().get("caseType"),
                roundTripped.getVariables().get("caseType"));
        }

        @Test
        void camundaProcess_ignoresUnknownAndKeepsCamelCase() throws Exception {
            String fixture = readFixture("jackson-roundtrip/camunda-process.json");

            CamundaProcess decoded = camundaMapper.readValue(fixture, CamundaProcess.class);
            assertEquals("proc-1", decoded.getId());
            assertEquals("wa.definition:1:abc", decoded.getDefinitionId());
            assertEquals("wa", decoded.getTenantId());
            assertEquals(Boolean.FALSE, decoded.getSuspended());

            String encoded = camundaMapper.writeValueAsString(decoded);
            assertContainsField(encoded, "definitionId");
            assertContainsField(encoded, "caseInstanceId");
            assertContainsField(encoded, "tenantId");
            assertDoesNotContainField(encoded, "definition_id");
            assertDoesNotContainField(encoded, "case_instance_id");
            assertDoesNotContainField(encoded, "unknownCamundaField");
        }

        @Test
        void sendMessageRequest_builtObjectUsesCamelCaseWireNames() throws Exception {
            SendMessageRequest request = new SendMessageRequest(
                "createTaskMessage",
                Map.of("caseId", DmnValue.dmnStringValue("1234567890123456")),
                null,
                false
            );

            String encoded = camundaMapper.writeValueAsString(request);
            assertContainsField(encoded, "messageName");
            assertContainsField(encoded, "processVariables");
            assertDoesNotContainField(encoded, "message_name");
            assertDoesNotContainField(encoded, "process_variables");

            SendMessageRequest decoded = camundaMapper.readValue(encoded, SendMessageRequest.class);
            assertEquals(request, decoded);
        }
    }

    @Nested
    @DisplayName("Task-management Feign (Jackson 3 snake_case)")
    class TaskManagementFeignPath {

        @Test
        void notesRequest_roundTripsFixtureWithSnakeCase() throws Exception {
            String fixture = readFixture("jackson-roundtrip/tma-notes-request.json");

            NotesRequest decoded = taskManagementMapper.readValue(fixture, NotesRequest.class);
            assertNotNull(decoded.getNoteResource());
            assertEquals(1, decoded.getNoteResource().size());
            NoteResource note = decoded.getNoteResource().getFirst();
            assertEquals("Code1", note.getCode());
            assertEquals("warning", note.getNoteType());
            assertEquals("someUserId", note.getUserId());
            assertEquals("Some warning text", note.getContent());

            String encoded = taskManagementMapper.writeValueAsString(decoded);
            assertContainsField(encoded, "notes");
            assertContainsField(encoded, "note_type");
            assertContainsField(encoded, "user_id");
            assertDoesNotContainField(encoded, "noteType");
            assertDoesNotContainField(encoded, "userId");
            assertDoesNotContainField(encoded, "noteResource");

            NotesRequest roundTripped = taskManagementMapper.readValue(encoded, NotesRequest.class);
            assertEquals(decoded, roundTripped);
        }

        @Test
        void notesRequest_builtObjectEmitsSnakeCaseNotCamelCase() throws Exception {
            NotesRequest request = new NotesRequest(List.of(
                new NoteResource("Code1", "warning", "someUserId", "Some warning text")
            ));

            String encoded = taskManagementMapper.writeValueAsString(request);
            assertContainsField(encoded, "note_type");
            assertContainsField(encoded, "user_id");
            assertDoesNotContainField(encoded, "noteType");
            assertDoesNotContainField(encoded, "userId");
        }
    }

    @Nested
    @DisplayName("MVC CreateTaskRequest (Jackson 3 Boot default)")
    class MvcPath {

        @Test
        void createTaskRequest_roundTripsFixture() throws Exception {
            String fixture = readFixture("jackson-roundtrip/mvc-create-task-request.json");

            CreateTaskRequest decoded = mvcMapper.readValue(fixture, CreateTaskRequest.class);
            assertEquals("1234567890123456", decoded.getCaseId());
            assertEquals("WA", decoded.getServiceDetails().getJurisdiction());
            assertEquals("WaCaseType", decoded.getServiceDetails().getCaseType());
            assertEquals("submitAppeal", decoded.getTransition().getEventId());
            assertNotNull(decoded.getDueDate());
            assertEquals(2020, decoded.getDueDate().getYear());
            assertEquals(9, decoded.getDueDate().getMonthValue());

            String encoded = mvcMapper.writeValueAsString(decoded);
            assertContainsField(encoded, "caseId");
            assertContainsField(encoded, "serviceDetails");
            assertContainsField(encoded, "dueDate");
            assertContainsField(encoded, "preState");
            assertContainsField(encoded, "eventId");
            assertDoesNotContainField(encoded, "case_id");
            assertDoesNotContainField(encoded, "due_date");
            assertDoesNotContainField(encoded, "service_details");

            CreateTaskRequest roundTripped = mvcMapper.readValue(encoded, CreateTaskRequest.class);
            assertEquals(decoded.getCaseId(), roundTripped.getCaseId());
            assertEquals(decoded.getServiceDetails(), roundTripped.getServiceDetails());
            assertEquals(decoded.getTransition(), roundTripped.getTransition());
            assertEquals(decoded.getDueDate().toInstant(), roundTripped.getDueDate().toInstant());
        }

        @Test
        void createTaskRequest_omitsNullDueDate() throws Exception {
            CreateTaskRequest request = new CreateTaskRequest(
                new ServiceDetails("WA", "WaCaseType"),
                "1234567890123456",
                new Transition("appealStarted", "submitAppeal", "appealSubmitted"),
                null
            );

            String encoded = mvcMapper.writeValueAsString(request);
            assertDoesNotContainField(encoded, "dueDate");
            assertContainsField(encoded, "caseId");
        }

        @Test
        void createTaskRequest_preservesDueDateInstantAcrossZones() {
            ZonedDateTime dueDate = ZonedDateTime.of(2020, 9, 5, 14, 47, 1, 250542000, ZoneOffset.ofHours(1));
            CreateTaskRequest request = new CreateTaskRequest(
                new ServiceDetails("WA", "WaCaseType"),
                "1234567890123456",
                new Transition("appealStarted", "submitAppeal", "appealSubmitted"),
                dueDate
            );

            String encoded = mvcMapper.writeValueAsString(request);
            CreateTaskRequest decoded = mvcMapper.readValue(encoded, CreateTaskRequest.class);
            assertEquals(dueDate.toInstant(), decoded.getDueDate().toInstant());
        }
    }

    @Nested
    @DisplayName("Warnings Jackson 2 bridge (temporary)")
    class WarningsJackson2BridgePath {

        @Test
        void warningValues_roundTripStillOwnedByJackson2ObjectMapper() throws Exception {
            // Intentional dual-stack: production WarningValues still uses Jackson 2 ObjectMapper.
            String json = "[{\"warningCode\":\"Code1\",\"warningText\":\"Text1\"}]";
            WarningValues warningValues = new WarningValues(json);

            assertEquals(1, warningValues.getValues().size());
            Warning warning = warningValues.getValues().getFirst();
            assertEquals("Code1", warning.getWarningCode());
            assertEquals("Text1", warning.getWarningText());
            assertEquals(json, warningValues.getValuesAsJson());
        }
    }

    private static String readFixture(String classpathLocation) throws IOException {
        try (InputStream inputStream = JacksonRoundTripTest.class
            .getClassLoader()
            .getResourceAsStream(classpathLocation)) {
            Objects.requireNonNull(inputStream, "Missing fixture: " + classpathLocation);
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertContainsField(String json, String fieldName) throws Exception {
        JsonNode tree = JsonMapper.shared().readTree(json);
        assertTrue(containsFieldName(tree, fieldName),
            () -> "Expected field '" + fieldName + "' in JSON: " + json);
    }

    private static void assertDoesNotContainField(String json, String fieldName) throws Exception {
        JsonNode tree = JsonMapper.shared().readTree(json);
        assertFalse(containsFieldName(tree, fieldName),
            () -> "Did not expect field '" + fieldName + "' in JSON: " + json);
    }

    private static boolean containsFieldName(JsonNode node, String fieldName) {
        if (node == null) {
            return false;
        }
        if (node.isObject()) {
            if (node.has(fieldName)) {
                return true;
            }
            for (JsonNode child : node) {
                if (containsFieldName(child, fieldName)) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                if (containsFieldName(child, fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
