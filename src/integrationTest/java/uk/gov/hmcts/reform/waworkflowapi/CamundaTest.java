package uk.gov.hmcts.reform.waworkflowapi;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRuleResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
public class CamundaTest {

    private DmnEngine dmnEngine;

    @BeforeEach
    void setUp() {
        dmnEngine = DmnEngineConfiguration
            .createDefaultDmnEngineConfiguration()
            .buildEngine();
    }

    @DisplayName("Get task id")
    @ParameterizedTest(name = "\"{0}\" \"{1}\" should go to \"{2}\"")
    @CsvSource({
        "submitAppeal, anything, processApplication",
        "submitTimeExtension, anything, decideOnTimeExtension",
        "uploadHomeOfficeBundle, awaitingRespondentEvidence, reviewRespondentEvidence",
        "submitCase, caseUnderReview, reviewAppealSkeletonArgument",
        "submitReasonsForAppeal, reasonsForAppealSubmitted, reviewReasonsForAppeal",
        "submitClarifyingQuestionAnswers, clarifyingQuestionsAnswersSubmitted, reviewClarifyingQuestionsAnswers",
        "submitCmaRequirements, cmaRequirementsSubmitted, reviewCmaRequirements",
        "listCma, cmaListed, attendCma",
        "uploadHomeOfficeAppealResponse, respondentReview, reviewRespondentResponse",
        "anything, prepareForHearing, createCaseSummary",
        "anything, finalBundling, createHearingBundle",
        "anything, preHearing, startDecisionsAndReasonsDocument"
    })
    public void shouldGetTaskIdTest(String eventId, String postState, String taskId) {
        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmn(eventId, postState);

        DmnDecisionRuleResult singleResult = dmnDecisionTableResult.getSingleResult();

        assertThat(singleResult.getEntry("taskId"), is(taskId));
    }

    @DisplayName("transition unmapped")
    @Test
    public void transitionUnampped() {
        DmnDecisionTableResult dmnDecisionRuleResults = evaluateDmn("anything", "anything");

        assertThat(dmnDecisionRuleResults.isEmpty(), is(true));
    }

    private DmnDecisionTableResult evaluateDmn(String eventId, String postState) {
        try (InputStream inputStream = Files.newInputStream(Paths.get("camunda/getTaskId.dmn"))) {
            DmnDecision decision = dmnEngine.parseDecision("getTask", inputStream);

            VariableMap variables = new VariableMapImpl();
            variables.putValue("eventId", eventId);
            variables.putValue("postEventState", postState);

            return dmnEngine.evaluateDecisionTable(decision, variables);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
