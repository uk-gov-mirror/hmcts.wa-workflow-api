package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class DmnResult<T> {

    private final T result;

    public DmnResult(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
}
