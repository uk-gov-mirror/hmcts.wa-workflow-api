package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Arrays;

public enum Task {
    PROCESS_APPLICATION("processApplication"),
    DECIDE_ON_TIME_EXTENSION("decideOnTimeExtension"),
    REVIEW_RESPONDENT_EVIDENCE("reviewRespondentEvidence"),
    REVIEW_APPEAL_SKELETON_ARGUMENT("reviewAppealSkeletonArgument"),
    REVIEW_REASONS_FOR_APPEAL("reviewReasonsForAppeal"),
    REVIEW_CLARIFYING_QUESTIONS_ANSWERS("reviewClarifyingQuestionsAnswers"),
    REVIEW_CMA_REQUIREMENTS("reviewCmaRequirements"),
    ATTEND_CMA("attendCma"),
    REVIEW_RESPONDENT_RESPONSE("reviewRespondentResponse"),
    CREATE_CASE_SUMMARY("createCaseSummary"),
    CREATE_HEARING_BUNDLE("createHearingBundle"),
    START_DECISIONS_AND_REASONS_DOCUMENT("startDecisionsAndReasonsDocument"),
    PROVIDE_RESPONDENT_EVIDENCE("provideRespondentEvidence"),
    PROVIDE_CASE_BUILDING("provideCaseBuilding"),
    PROVIDE_REASONS_FOR_APPEAL("provideReasonsForAppeal"),
    PROVIDE_CLARIFYING_ANSWERS("provideClarifyingAnswers"),
    PROVIDE_CMA_REQUIREMENTS("provideCmaRequirements"),
    PROVIDE_RESPONDENT_REVIEW("provideRespondentReview"),
    PROVIDE_HEARING_REQUIREMENTS("provideHearingRequirements")
    ;


    private final String id;

    Task(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Task taskForId(String id) {
        return Arrays.stream(values())
            .filter(value -> value.id.equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("[" + id + "] does not map to a Task id"));
    }
}
