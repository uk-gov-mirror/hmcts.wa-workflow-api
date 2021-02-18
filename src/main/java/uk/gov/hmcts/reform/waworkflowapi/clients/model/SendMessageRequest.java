package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

@EqualsAndHashCode
@ToString
public class SendMessageRequest {
    private String businessKey;
    private final String messageName;
    private final Map<String, DmnValue<?>> processVariables;
    private final Map<String, DmnValue<?>> correlationKeys;

    @JsonCreator
    public SendMessageRequest(@JsonProperty("messageName") String messageName,
                              @JsonProperty("processVariables") Map<String, DmnValue<?>> processVariables,
                              @JsonProperty("correlationKeys") Map<String, DmnValue<?>> correlationKeys) {
        this.messageName = messageName;
        this.processVariables = processVariables;
        this.correlationKeys = correlationKeys;
    }

    public SendMessageRequest(String businessKey,
                              String messageName,
                              Map<String, DmnValue<?>> processVariables,
                              Map<String, DmnValue<?>> correlationKeys) {
        this.businessKey = businessKey;
        this.messageName = messageName;
        this.processVariables = processVariables;
        this.correlationKeys = correlationKeys;
    }

    public String getMessageName() {
        return messageName;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public Map<String, DmnValue<?>> getProcessVariables() {
        return processVariables;
    }

    public Map<String, DmnValue<?>> getCorrelationKeys() {
        return correlationKeys;
    }
}
