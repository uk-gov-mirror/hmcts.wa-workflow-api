package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class DmnValue<T> {
    private T value;
    private String type;

    public static DmnValue<String> dmnStringValue(String value) {
        return new DmnValue<>(value, "String");
    }

    public static DmnValue<Integer> dmnIntegerValue(Integer value) {
        return new DmnValue<>(value, "Integer");
    }

    private DmnValue() {
    }

    public DmnValue(T value, String type) {
        this.value = value;
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }
        DmnValue dmnValue = (DmnValue) anotherObject;
        return Objects.equals(value, dmnValue.value)
               && Objects.equals(type, dmnValue.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return "DmnValue{"
               + "value='" + value + '\''
               + ", type='" + type + '\''
               + '}';
    }
}
