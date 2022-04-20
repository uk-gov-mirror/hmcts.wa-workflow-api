package uk.gov.hmcts.reform.waworkflowapi.exceptions;

import org.zalando.problem.AbstractThrowableProblem;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.enums.ErrorMessages;

import java.net.URI;

import static org.zalando.problem.Status.FORBIDDEN;

@SuppressWarnings("java:S110")
public class GenericForbiddenException extends AbstractThrowableProblem {

    private static final long serialVersionUID = 1L;

    private static final URI TYPE = URI.create("https://github.com/hmcts/wa-workflow-api/problem/forbidden");
    private static final String TITLE = "Forbidden";

    public GenericForbiddenException(ErrorMessages message) {
        super(TYPE, TITLE, FORBIDDEN, message.getDetail());
    }
}
