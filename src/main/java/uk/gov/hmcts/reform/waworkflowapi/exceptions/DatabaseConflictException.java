package uk.gov.hmcts.reform.waworkflowapi.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.enums.ErrorMessages;

import java.net.URI;

import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;

@SuppressWarnings("java:S110")
public class DatabaseConflictException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private static final URI TYPE = URI.create("https://github.com/hmcts/wa-workflow-api/problem/database-conflict");
    private static final String TITLE = "Database Conflict Error";

    public DatabaseConflictException(ErrorMessages message) {
        super(TYPE, TITLE, SERVICE_UNAVAILABLE, message.getDetail());
    }
}
