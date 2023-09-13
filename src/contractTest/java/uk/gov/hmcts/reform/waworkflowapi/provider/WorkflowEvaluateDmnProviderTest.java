package uk.gov.hmcts.reform.waworkflowapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.EvaluateDmnService;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.SendMessageService;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskController;
import uk.gov.hmcts.reform.waworkflowapi.provider.service.WorkflowProviderTestConfiguration;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue.dmnStringValue;


@ExtendWith(SpringExtension.class)
@Provider("wa_workflow_api_evaluate_dmn")
//Uncomment this and comment the @PacBroker line to test WorkflowEvaluateDmnConsumerTest local consumer.
//@PactFolder("target/pacts")
@PactBroker(
    scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:9292}",
    consumerVersionSelectors = {
        @VersionSelector(tag = "master")}
)
@Import(WorkflowProviderTestConfiguration.class)
@IgnoreNoPactsToVerify
public class WorkflowEvaluateDmnProviderTest {

    @Mock
    private EvaluateDmnService evaluateDmnService;

    @Mock
    private SendMessageService sendMessageService;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(new CreateTaskController(
            evaluateDmnService,
            sendMessageService
        ));
        if (context != null) {
            context.setTarget(testTarget);
        }

    }

    @State({"evaluate dmn"})
    public void evaluateDmn() {
        setInitMock();
    }

    private void setInitMock() {
        final List<Map<String, DmnValue<?>>> dmnValue = List.of(
            Map.of(
                "name", dmnStringValue("some name"),
                "jurisdiction", dmnStringValue("WA"),
                "caseType", dmnStringValue("WaCaseType"),
                "taskId", dmnStringValue("some taskId"),
                "caseId", dmnStringValue("some caseId")
            )
        );
        when(evaluateDmnService.evaluateDmn(any(), anyString(), anyString())).thenReturn(dmnValue);
    }
}
