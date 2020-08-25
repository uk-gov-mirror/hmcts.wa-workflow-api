package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskManagerService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.Task.PROCESS_APPLICATION;

@WebMvcTest
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CreateTaskTest {

    @Autowired
    private transient MockMvc mockMvc;

    @MockBean
    private TaskManagerService taskManagerService;

    @DisplayName("Should create task with 201 response")
    @Test
    public void createsTaskForTransition() throws Exception {
        CreateTaskRequest createTaskRequest = appealSubmittedCreateTaskRequest();
        Transition transition = createTaskRequest.getTransition();
        when(taskManagerService.getTask(transition)).thenReturn(Optional.of(PROCESS_APPLICATION));
        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(createTaskRequest))
        ).andExpect(status().isCreated()).andReturn();
    }

    @DisplayName("Should not create task with 204 response")
    @Test
    public void doesNotCreateTaskForTransition() throws Exception {
        CreateTaskRequest createTaskRequest = appealSubmittedCreateTaskRequest();
        Transition transition = createTaskRequest.getTransition();
        when(taskManagerService.getTask(transition)).thenReturn(Optional.empty());
        mockMvc.perform(
            post("/tasks")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(asJsonString(createTaskRequest))
        ).andExpect(status().isNoContent()).andReturn();
    }
}
