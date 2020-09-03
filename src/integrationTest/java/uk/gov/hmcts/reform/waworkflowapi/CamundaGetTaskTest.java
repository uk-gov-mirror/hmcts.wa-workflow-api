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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class CamundaGetTaskTest {

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
        "submitAppeal, anything, processApplication, TCW",
        "submitTimeExtension, anything, decideOnTimeExtension, TCW",
        "uploadHomeOfficeBundle, awaitingRespondentEvidence, reviewRespondentEvidence, TCW",
        "submitCase, caseUnderReview, reviewAppealSkeletonArgument, TCW",
        "submitReasonsForAppeal, reasonsForAppealSubmitted, reviewReasonsForAppeal, TCW",
        "submitClarifyingQuestionAnswers, clarifyingQuestionsAnswersSubmitted, reviewClarifyingQuestionsAnswers, TCW",
        "submitCmaRequirements, cmaRequirementsSubmitted, reviewCmaRequirements, TCW",
        "listCma, cmaListed, attendCma, TCW",
        "uploadHomeOfficeAppealResponse, respondentReview, reviewRespondentResponse, TCW",
        "anything, prepareForHearing, createCaseSummary, TCW",
        "anything, finalBundling, createHearingBundle, TCW",
        "anything, preHearing, startDecisionsAndReasonsDocument, TCW",
        "requestRespondentEvidence, awaitingRespondentEvidence, provideRespondentEvidence, external",
        "requestCaseBuilding, caseBuilding, provideCaseBuilding, external",
        "requestReasonsForAppeal, awaitingReasonsForAppeal, provideReasonsForAppeal, external",
        "sendDirectionWithQuestions, awaitingClarifyingQuestionsAnswers, provideClarifyingAnswers, external",
        "requestCmaRequirements, awaitingCmaRequirements, provideCmaRequirements, external",
        "requestRespondentReview, respondentReview, provideRespondentReview, external",
        "requestHearingRequirements, submitHearingRequirements, provideHearingRequirements, external"
    })
    void shouldGetTaskIdTest(String eventId, String postState, String taskId, String group) {
        DmnDecisionTableResult dmnDecisionTableResult = evaluateDmn(eventId, postState);

        DmnDecisionRuleResult singleResult = dmnDecisionTableResult.getSingleResult();

        assertThat(singleResult.getEntry("taskId"), is(taskId));
        assertThat(singleResult.getEntry("group"), is(group));
    }

    @DisplayName("transition unmapped")
    @Test
    void transitionUnmapped() {
        DmnDecisionTableResult dmnDecisionRuleResults = evaluateDmn("anything", "anything");

        assertThat(dmnDecisionRuleResults.isEmpty(), is(true));
    }

    private DmnDecisionTableResult evaluateDmn(String eventId, String postState) {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("getTaskId.dmn")) {
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
