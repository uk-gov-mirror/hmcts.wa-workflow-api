package uk.gov.hmcts.reform.waworkflowapi.exceptions;

public class IdempotencyTaskWorkerException extends RuntimeException {
    private static final long serialVersionUID = 4259698709568631855L;

    public IdempotencyTaskWorkerException(String message) {
        super(message);
    }
}
