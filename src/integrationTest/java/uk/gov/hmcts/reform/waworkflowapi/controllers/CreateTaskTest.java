package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.SpringBootIntegrationBaseTest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.SendMessageRequest;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnIntegerValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;

class CreateTaskTest extends SpringBootIntegrationBaseTest {

    @Autowired
    private transient MockMvc mockMvc;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private CamundaClient camundaClient;

    private static final String BEARER_SERVICE_TOKEN = "Bearer service token";

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(BEARER_SERVICE_TOKEN);
    }

    @DisplayName("Should evaluate a DMN and return a 200")
    @Test
    void evaluateDmn() throws Exception {

        EvaluateDmnRequest evaluateDmnRequest = new EvaluateDmnRequest(
            Map.of("name", dmnStringValue("Process Application"),
                   "workingDaysAllowed", dmnIntegerValue(2),
                   "taskId", dmnStringValue("processApplication"),
                   "group", dmnStringValue("TCW")));

        when(camundaClient.evaluateDmn(
            BEARER_SERVICE_TOKEN,
            anyString(),
            evaluateDmnRequest
        )).thenReturn(getEvalResponse());

        mockMvc.perform(
            post("/workflow/decision-definition/getTask_IA_asylum/evaluate")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(asJsonString(evaluateDmnRequest))
        ).andExpect(status().isOk()).andReturn();

    }

    @DisplayName("Should send message to camunda")
    @Test
    void shouldSendAnMessageToCamunda() throws Exception {

        SendMessageRequest sendMessageRequest = new SendMessageRequest(
            "createTaskMessage",
            Map.of(
                "name",dmnStringValue("name"),
                "group",dmnStringValue("group"),
                "jurisdiction",dmnStringValue("IA"),
                "caseType",dmnStringValue("Asylum"),
                "taskId",dmnStringValue("provideRespondentEvidence")
            )
        );

        mockMvc.perform(
            post("/workflow/message")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(sendMessageRequest))
        ).andExpect(status().isNoContent()).andReturn();

    }

    private List<Map<String,DmnValue>> getEvalResponse() {
        return List.of(Map.of(
                                 "name",dmnStringValue("processApplication"),
                                 "group", dmnStringValue("TCW"),
                                 "workingDaysAllowed", dmnIntegerValue(5),
                                 "taskId", dmnStringValue("task name")
                             )
        );
    }
}
