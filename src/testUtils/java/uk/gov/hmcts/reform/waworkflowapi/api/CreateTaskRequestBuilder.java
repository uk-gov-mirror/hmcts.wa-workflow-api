package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class CreateTaskRequestBuilder {
    private String caseId;
    private Transition transition;
    private String dueDate;

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

    public CreateTaskRequestBuilder withDueDate(ZonedDateTime dueDate) {
        this.dueDate = dueDate.format(DateTimeFormatter.ISO_INSTANT);
        return this;
    }

    public uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequest build() {
        return new CreateTaskRequest(caseId, transition, dueDate);
    }
}
