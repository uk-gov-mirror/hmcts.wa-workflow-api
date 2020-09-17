package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

public class CreateTaskRequest {
    private final String caseId;
    private final Transition transition;
    private final String dueDate;

    public CreateTaskRequest(String caseId, Transition transition, String dueDate) {
        this.caseId = caseId;
        this.transition = transition;
        this.dueDate = dueDate;
    }

    public String getCaseId() {
        return caseId;
    }

    public Transition getTransition() {
        return transition;
    }

    public String getDueDate() {
        return dueDate;
    }
}
