package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class DmnResult<T> {

    private final T result;

    public DmnResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }
        DmnResult<?> dmnResult = (DmnResult<?>) anotherObject;
        return Objects.equals(result, dmnResult.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result);
    }

    @Override
    public String toString() {
        return "DmnResult{"
               + "result=" + result
               + '}';
    }
}
