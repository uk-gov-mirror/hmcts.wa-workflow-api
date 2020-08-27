package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.GetTaskDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.GetTaskDmnResult;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.PROCESS_APPLICATION;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class CreateTaskTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private CamundaClient camundaClient;

    @DisplayName("Should create task with 201 response")
    @Test
    void createsTaskForTransition() throws Exception {
        CreateTaskRequest createTaskRequest = appealSubmittedCreateTaskRequest();
        when(camundaClient.getTask(createGetTaskDmnRequest(createTaskRequest)))
            .thenReturn(createGetTaskResponse());
        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(createTaskRequest))
        ).andExpect(status().isCreated()).andReturn();
    }

    @DisplayName("Should not create task with 204 response")
    @Test
    void doesNotCreateTaskForTransition() throws Exception {
        CreateTaskRequest createTaskRequest = appealSubmittedCreateTaskRequest();
        when(camundaClient.getTask(createGetTaskDmnRequest(createTaskRequest)))
            .thenReturn(emptyList());
        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(createTaskRequest))
        ).andExpect(status().isNoContent()).andReturn();
    }

    private DmnRequest<GetTaskDmnRequest> createGetTaskDmnRequest(CreateTaskRequest createTaskRequest) {
        return new DmnRequest<>(
            new GetTaskDmnRequest(
                dmnStringValue(createTaskRequest.getTransition().getEventId()),
                dmnStringValue(createTaskRequest.getTransition().getPostState())
            )
        );
    }

    private List<GetTaskDmnResult> createGetTaskResponse() {
        return singletonList(new GetTaskDmnResult(dmnStringValue(PROCESS_APPLICATION.getId())));
    }
}
