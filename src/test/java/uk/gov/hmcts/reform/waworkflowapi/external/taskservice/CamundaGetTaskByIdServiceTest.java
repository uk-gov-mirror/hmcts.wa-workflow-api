package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.waworkflowapi.camuda.rest.api.wrapper.CamundaTaskService;
import uk.gov.hmcts.reform.waworkflowapi.camuda.rest.api.wrapper.CamundaTaskServiceWrapper;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
@RunWith(MockitoJUnitRunner.class)
class CamundaGetTaskByIdServiceTest {

    @InjectMocks
    CamundaTaskService camundaTaskService;

    @MockBean
    private CamundaTaskServiceWrapper camundaTaskServiceWrapper;


    @BeforeEach
    void setUp() {
        camundaTaskServiceWrapper = mock(CamundaTaskServiceWrapper.class);
        camundaTaskService = new CamundaTaskService(camundaTaskServiceWrapper);
    }

    @Test
    void createsATask() {
        when(camundaTaskService.getTask("TestId")).thenReturn("testString");
        String task = camundaTaskService.getTask("TestId");
        when(camundaTaskServiceWrapper.getTask("TestId")).thenReturn("testString");
        assertEquals(task, "testString");
    }
}
