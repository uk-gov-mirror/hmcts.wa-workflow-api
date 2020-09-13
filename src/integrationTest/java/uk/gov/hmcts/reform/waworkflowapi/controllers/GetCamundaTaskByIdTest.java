package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.GetTaskController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class GetCamundaTaskByIdTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    GetTaskController getTaskController;

    @Test
    void taskbyIdTest() throws Exception {
        when(getTaskController.getTask("025c59e3-dbe2-11ea-81e2-661816095024"))
            .thenReturn("TestResponse");
        mockMvc.perform(get("/task/025c59e3-dbe2-11ea-81e2-661816095024")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void noIdReturned() throws Exception {
        mockMvc.perform(get("/test/{task-id}", 34)
                            .accept(MediaType.APPLICATION_JSON))
                            .andExpect(status().isNotFound());
    }

}
