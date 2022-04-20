package uk.gov.hmcts.reform.waworkflowapi.exceptions;

public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConflictException(
        String message,
        Throwable cause
    ) {
        super(message, cause);
    }
}
