package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModelProperty;

import java.time.ZonedDateTime;
import java.util.Objects;

public class CreateTaskRequest {
    @ApiModelProperty
    private final ServiceDetails serviceDetails;
    @ApiModelProperty(example = "abc1234567890", required = true, notes = "The case id in CCD")
    private final String caseId;
    @ApiModelProperty(required = true)
    private final Transition transition;
    @JsonInclude(Include.NON_NULL)
    @ApiModelProperty(
        example = "2020-09-05T14:47:01.250542+01:00",
        notes = "Optional due date for the task that will be created")
    private final ZonedDateTime dueDate;

    public CreateTaskRequest(
        ServiceDetails serviceDetails,
        String caseId,
        Transition transition,
        ZonedDateTime dueDate
    ) {
        this.serviceDetails = serviceDetails;
        this.caseId = caseId;
        this.transition = transition;
        this.dueDate = dueDate;
    }

    public ServiceDetails getServiceDetails() {
        return serviceDetails;
    }

    public String getCaseId() {
        return caseId;
    }

    public Transition getTransition() {
        return transition;
    }

    public ZonedDateTime getDueDate() {
        return dueDate;
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
        return Objects.equals(serviceDetails, that.serviceDetails)
               && Objects.equals(caseId, that.caseId)
               && Objects.equals(transition, that.transition)
               && Objects.equals(dueDate, that.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseId, transition);
    }

    @Override
    public String toString() {
        return "CreateTaskRequest{"
               + "serviceDetails='" + serviceDetails + '\''
               + ", caseId='" + caseId + '\''
               + ", transition=" + transition
               + ", dueDate=" + dueDate
               + '}';
    }
}
