package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class AddProcessVariableRequest {
    private final Map<String, DmnValue<String>> modifications;

    public AddProcessVariableRequest(Map<String, DmnValue<String>> modifications) {
        this.modifications = modifications;
    }

    public Map<String, DmnValue<String>> getModifications() {
        return modifications;
    }
}
