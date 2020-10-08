package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;
import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

public class CreateTaskRequest {
    private final ServiceDetails serviceDetails;
    private final String caseId;
    private final Transition transition;
    private final String dueDate;

    public CreateTaskRequest(ServiceDetails serviceDetails, String caseId, Transition transition, String dueDate) {
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

    public String getDueDate() {
        return dueDate;
    }
}
