package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.ServiceDetails;

import java.util.Map;

public class EvaluateDmnRequest {

    private Map<String, DmnValue> variables;
    private ServiceDetails serviceDetails;

    public EvaluateDmnRequest() {
        // Empty constructor
    }

    public EvaluateDmnRequest(Map<String, DmnValue> variables, ServiceDetails serviceDetails) {
        this.variables = variables;
        this.serviceDetails = serviceDetails;
    }

    public Map<String, DmnValue> getVariables() {
        return variables;
    }

    public ServiceDetails getServiceDetails() {
        return serviceDetails;
    }
}
