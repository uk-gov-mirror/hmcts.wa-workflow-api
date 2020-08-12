package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

public final class CreateTaskRequestBuilder {
    private String caseId;
    private Transition transition;

    private CreateTaskRequestBuilder() {
    }

    public static CreateTaskRequestBuilder aCreateTaskRequest() {
        return new CreateTaskRequestBuilder();
    }

    public CreateTaskRequestBuilder withCaseId(String caseId) {
        this.caseId = caseId;
        return this;
    }

    public CreateTaskRequestBuilder withTransition(Transition transition) {
        this.transition = transition;
        return this;
    }

    public CreateTaskRequest build() {
        return new CreateTaskRequest(caseId, transition);
    }
}
