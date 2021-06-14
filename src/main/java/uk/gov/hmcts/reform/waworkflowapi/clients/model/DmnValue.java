package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class DmnValue<T> {
    private T value;
    private String type;

    private DmnValue() {
    }

    public DmnValue(T value, String type) {
        this.value = value;
        this.type = type;
    }

    public static DmnValue<String> dmnStringValue(String value) {
        return new DmnValue<>(value, "String");
    }

    public static DmnValue<Integer> dmnIntegerValue(Integer value) {
        return new DmnValue<>(value, "Integer");
    }

    public T getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

}
