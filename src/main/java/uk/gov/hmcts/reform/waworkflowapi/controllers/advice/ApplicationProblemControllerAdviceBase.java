package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.exc.MismatchedInputException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.enums.ErrorMessages;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

public interface ApplicationProblemControllerAdviceBase {

    String EXCEPTION_OCCURRED = "Exception occurred: {}";

    default ResponseEntity<ThrowableProblem> createDownStreamErrorResponse(Status statusType) {
        URI type = URI.create("https://github.com/hmcts/wa-workflow-api/problem/downstream-dependency-error");
        String title = "Downstream Dependency Error";
        ErrorMessages detail = ErrorMessages.DOWNSTREAM_DEPENDENCY_ERROR;

        return ResponseEntity.status(statusType.getStatusCode())
            .header(CONTENT_TYPE, APPLICATION_PROBLEM_JSON_VALUE)
            .body(Problem.builder()
                .withType(type)
                .withTitle(title)
                .withDetail(detail.getDetail())
                .withStatus(statusType)
                .build());
    }


    /**
     * Common handling of JSON parsing/mapping exceptions.Avoids having to return error
     * details with internal Java package/class names.
     */
    default String extractErrors(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof StreamReadException sre) {
            return sre.getOriginalMessage();
        }
        return processInputException(cause).orElseGet(() -> "Invalid request message");
    }

    default Optional<String> processInputException(Throwable cause) {
        if (cause instanceof MismatchedInputException) {
            return processMisMatchedInputException(cause);
        } else if (cause instanceof DatabindException) {
            return processDatabindException(cause);
        }
        return Optional.empty();
    }

    default Optional<String> processMisMatchedInputException(Throwable cause) {
        MismatchedInputException mie = (MismatchedInputException) cause;
        if (mie.getPath() != null && !mie.getPath().isEmpty()) {
            String fieldName = mie.getPath().stream()
                .map(ref -> ref.getPropertyName() == null ? "[0]" : ref.getPropertyName())
                .collect(Collectors.joining("."));
            return Optional.of("Invalid request field: " + fieldName);
        }
        return Optional.empty();
    }

    default Optional<String> processDatabindException(Throwable cause) {
        DatabindException dbe = (DatabindException) cause;
        if (dbe.getPath() != null && !dbe.getPath().isEmpty()) {
            String fieldName = dbe.getPath().stream()
                .map(ref -> ref.getPropertyName() == null ? "[0]" : ref.getPropertyName())
                .collect(Collectors.joining("."));
            return Optional.of("Invalid request field: "
                               + fieldName
                               + ": "
                               + dbe.getOriginalMessage());
        }
        return Optional.empty();
    }

}
