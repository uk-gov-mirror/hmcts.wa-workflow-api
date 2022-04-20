package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.web.advice.validation.ValidationAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.GenericForbiddenException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.GenericServerErrorException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.enums.ErrorMessages;

import java.net.URI;
import java.util.List;
import javax.validation.ConstraintViolationException;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.UNSUPPORTED_MEDIA_TYPE;

@Slf4j
@ControllerAdvice(basePackages = {
    "uk.gov.hmcts.reform.waworkflowapi.controllers"
})
@RequestMapping(produces = APPLICATION_PROBLEM_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.DataflowAnomalyAnalysis",
    "PMD.UseStringBufferForStringAppends", "PMD.LawOfDemeter"})
public class ApplicationProblemControllerAdvice implements ValidationAdviceTrait,
    ApplicationProblemControllerAdviceBase {

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ThrowableProblem> notAcceptableMediaTypeHandler(HttpMediaTypeNotAcceptableException exception) {

        Status statusType = UNSUPPORTED_MEDIA_TYPE; //415
        URI type = URI.create("https://github.com/hmcts/wa-workflow-api/problem/unsupported-media-type");
        String title = "Unsupported Media Type";

        ErrorMessages detail = ErrorMessages.UNSUPPORTED_MEDIA_TYPE;

        return ResponseEntity.status(statusType.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(Problem.builder()
                .withType(type)
                .withTitle(title)
                .withDetail(detail.getDetail())
                .withStatus(statusType)
                .build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Problem> handleMessageNotReadable(HttpMessageNotReadableException exception) {

        Status statusType = BAD_REQUEST; //400
        URI type = URI.create("https://github.com/hmcts/wa-workflow-api/problem/bad-request");
        String title = "Bad Request";

        String errorMessage = extractErrors(exception);
        return ResponseEntity.status(statusType.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(Problem.builder()
                .withType(type)
                .withTitle(title)
                .withDetail(errorMessage)
                .withStatus(statusType)
                .build());
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                NativeWebRequest request) {

        Status status = BAD_REQUEST; //400
        URI type = URI.create("https://github.com/hmcts/wa-workflow-api/problem/constraint-validation");

        List<Violation> streamViolations = createViolations(ex.getBindingResult());

        List<Violation> violations = streamViolations.stream()
            // sorting to make tests deterministic
            .sorted(comparing(Violation::getField).thenComparing(Violation::getMessage))
            .collect(toList());

        return ResponseEntity.status(status.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(new ConstraintViolationProblem(
                type,
                status,
                violations)
            );

    }

    @Override
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Problem> handleConstraintViolation(
        ConstraintViolationException ex,
        NativeWebRequest request) {
        Status status = BAD_REQUEST; //400
        URI type = URI.create("https://github.com/hmcts/wa-workflow-api/problem/constraint-validation");

        final List<Violation> violations = ex.getConstraintViolations().stream()
            .map(this::createViolation)
            .collect(toList());

        return ResponseEntity.status(status.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(new ConstraintViolationProblem(
                type,
                status,
                violations)
            );
    }

    @ExceptionHandler({
        GenericForbiddenException.class,
        GenericServerErrorException.class
    })
    protected ResponseEntity<Problem> handleApplicationProblemExceptions(
        AbstractThrowableProblem ex
    ) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatus().getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(Problem.builder()
                .withType(ex.getType())
                .withTitle(ex.getTitle())
                .withDetail(ex.getMessage())
                .withStatus(ex.getStatus())
                .build());
    }

}

