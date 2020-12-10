package uk.gov.hmcts.reform.waworkflowapi.controllers;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootIntegrationBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.duedate.DateService;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageRequest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnIntegerValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;

@Slf4j
class CreateTaskControllerTest extends SpringBootIntegrationBaseTest {

    public static final String WORKFLOW_MESSAGE_ENDPOINT = "/workflow/message";
    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";
    public static final String FIXED_DATE = "2020-12-14T10:24:38.975296Z";

    @Autowired
    private transient MockMvc mockMvc;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private CamundaClient camundaClient;
    @MockBean
    private DateService dateService;

    @Captor
    private ArgumentCaptor<SendMessageRequest> captor;


    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);

        ZonedDateTime nowDateMock = ZonedDateTime.parse(FIXED_DATE);
        when(dateService.now()).thenReturn(nowDateMock);
    }

    @DisplayName("Should evaluate a DMN and return a 200")
    @Test
    void evaluateDmn() throws Exception {

        EvaluateDmnRequest evaluateDmnRequest = new EvaluateDmnRequest(
            Map.of("name", dmnStringValue("Process Application"),
                   "workingDaysAllowed", dmnIntegerValue(2),
                   "taskId", dmnStringValue("processApplication"),
                   "group", dmnStringValue("TCW")
            ));

        when(camundaClient.evaluateDmn(
            eq(BEARER_SERVICE_TOKEN),
            anyString(),
            eq(evaluateDmnRequest)
        )).thenReturn(getEvalResponse());

        mockMvc.perform(
            post("/workflow/decision-definition/key/getTask_IA_asylum/evaluate")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(evaluateDmnRequest))
        ).andExpect(status().isOk()).andReturn();

    }

    @ParameterizedTest
    @MethodSource("scenarioProvider")
    void sendMessage(Scenario scenario) throws Exception {
        mockMvc.perform(
            post(WORKFLOW_MESSAGE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(scenario.sendMessageRequest))
        ).andExpect(status().isNoContent()).andReturn();

        verify(camundaClient).sendMessage(
            eq(BEARER_SERVICE_TOKEN),
            captor.capture()
        );
        SendMessageRequest actualSendMessageRequest = captor.getValue();

        assertThat(actualSendMessageRequest).isEqualTo(scenario.expectedSendMessageRequest);
    }

    private static Stream<Scenario> scenarioProvider() {

        /*
         Scenario1: When messageName is other than createTaskMessage
                    Then message is sent to Camunda with no dueDate
         */

        SendMessageRequest sendMessageRequest1 = new SendMessageRequest(
            "some other message",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        SendMessageRequest expectedSendMessageRequest1 = new SendMessageRequest(
            "some other message",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        Scenario messageIsOtherThanCreateTaskThenDueTaskIsNotSet = Scenario.builder()
            .sendMessageRequest(sendMessageRequest1)
            .expectedSendMessageRequest(expectedSendMessageRequest1)
            .build();

        /*
        Scenario2: When messageName is createTaskMessage and dueDate is not null
                   Then message is sent to Camunda with the given dueDate
         */

        SendMessageRequest sendMessageRequest2 = new SendMessageRequest(
            "createTaskMessage",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "dueDate", dmnStringValue(FIXED_DATE),
                "workingDaysAllowed", dmnIntegerValue(0),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        SendMessageRequest expectedSendMessageRequest2 = new SendMessageRequest(
            "createTaskMessage",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "dueDate", dmnStringValue(FIXED_DATE),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        Scenario messageIsCreateTaskThenDueTaskIsPastToCamunda = Scenario.builder()
            .sendMessageRequest(sendMessageRequest2)
            .expectedSendMessageRequest(expectedSendMessageRequest2)
            .build();

        /*
         Scenario3: When messageName is createTaskMessage and dueDate is null
                   Then message is sent to Camunda with the default dueDate
                   And it is calculated using the workingDaysAllowed field
         */

        SendMessageRequest sendMessageRequest3 = new SendMessageRequest(
            "createTaskMessage",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "workingDaysAllowed", dmnIntegerValue(2),
                "dueDate", dmnStringValue(null),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        String expectedDueDate = ZonedDateTime.parse(FIXED_DATE).plusDays(2).format(DateTimeFormatter.ISO_INSTANT);
        SendMessageRequest expectedSendMessageRequest3 = new SendMessageRequest(
            "createTaskMessage",
            Map.of(
                "name", dmnStringValue("some name"),
                "group", dmnStringValue("some group"),
                "jurisdiction", dmnStringValue("ia"),
                "caseType", dmnStringValue("asylum"),
                "taskId", dmnStringValue("some taskId"),
                "dueDate", dmnStringValue(expectedDueDate),
                "caseReference", dmnStringValue("some caseReference")
            )
        );

        Scenario messageIsCreateTaskThenDueTaskIsCalculated = Scenario.builder()
            .sendMessageRequest(sendMessageRequest3)
            .expectedSendMessageRequest(expectedSendMessageRequest3)
            .build();

        return Stream.of(
            messageIsOtherThanCreateTaskThenDueTaskIsNotSet,
            messageIsCreateTaskThenDueTaskIsPastToCamunda,
            messageIsCreateTaskThenDueTaskIsCalculated
        );
    }

    @Builder
    private static class Scenario {
        private final SendMessageRequest sendMessageRequest;
        private final SendMessageRequest expectedSendMessageRequest;
    }

    private List<Map<String, DmnValue<?>>> getEvalResponse() {
        return List.of(Map.of(
            "name", dmnStringValue("processApplication"),
            "group", dmnStringValue("TCW"),
            "workingDaysAllowed", dmnIntegerValue(5),
            "taskId", dmnStringValue("task name")
                       )
        );
    }

}
