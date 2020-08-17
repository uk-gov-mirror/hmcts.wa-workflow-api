package uk.gov.hmcts.reform.waworkflowapi.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestCreator.appealSubmittedCreateTaskRequestString;

@WebMvcTest
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class CreateTaskTest {

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Should welcome upon root request with 200 response code")
    @Test
    public void welcomeRootEndpoint() throws Exception {
        mockMvc.perform(
            post("/tasks")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(appealSubmittedCreateTaskRequestString())
        ).andExpect(status().isNoContent()).andReturn();
    }
}
