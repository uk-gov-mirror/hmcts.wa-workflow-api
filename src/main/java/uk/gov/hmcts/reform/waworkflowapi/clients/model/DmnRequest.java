package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import java.util.Objects;

public class DmnRequest<T> {
    private final T variables;

    public DmnRequest(T variables) {
        this.variables = variables;
    }

    public T getVariables() {
        return variables;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }
        DmnRequest<?> that = (DmnRequest<?>) anotherObject;
        return Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variables);
    }

    @Override
    public String toString() {
        return "DmnRequest{"
               + "variables=" + variables
               + '}';
    }
}
