package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class CreateTaskRequest {
    @ApiModelProperty(example = "abc1234567890", required = true, notes = "The case id in CCD")
    private final String caseId;
    private final Transition transition;

    public CreateTaskRequest(String caseId, Transition transition) {
        this.caseId = caseId;
        this.transition = transition;
    }

    public String getCaseId() {
        return caseId;
    }

    public Transition getTransition() {
        return transition;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        CreateTaskRequest that = (CreateTaskRequest) object;
        return Objects.equals(caseId, that.caseId)
               && Objects.equals(transition, that.transition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, transition);
    }

    @Override
    public String toString() {
        return "CreateTaskRequest{"
               + "caseId='" + caseId + '\''
               + ", transition=" + transition
               + '}';
    }
}
