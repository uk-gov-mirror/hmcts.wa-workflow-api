package uk.gov.hmcts.reform.waworkflowapi.clients.model;


import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class EvaluateDmnRequest {

    private Map<String, DmnValue<?>> variables;

    public EvaluateDmnRequest() {
        // Empty constructor
    }

    public EvaluateDmnRequest(Map<String, DmnValue<?>> variables) {
        this.variables = variables;
    }

    public Map<String, DmnValue<?>> getVariables() {
        return variables;
    }

}
