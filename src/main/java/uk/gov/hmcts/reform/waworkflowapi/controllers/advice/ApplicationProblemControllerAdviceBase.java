package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
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
        if (cause instanceof JsonParseException jpe) {
            return jpe.getOriginalMessage();
        }
        return processInputException(cause).orElseGet(() -> "Invalid request message");
    }

    default Optional<String> processInputException(Throwable cause) {
        if (cause instanceof MismatchedInputException) {
            return processMisMatchedInputException(cause);
        } else if (cause instanceof JsonMappingException) {
            return processJsonMappingException(cause);
        }
        return Optional.empty();
    }

    default Optional<String> processMisMatchedInputException(Throwable cause) {
        MismatchedInputException mie = (MismatchedInputException) cause;
        if (mie.getPath() != null && !mie.getPath().isEmpty()) {
            String fieldName = mie.getPath().stream()
                .map(ref -> ref.getFieldName() == null ? "[0]" : ref.getFieldName())
                .collect(Collectors.joining("."));
            return Optional.of("Invalid request field: " + fieldName);
        }
        return Optional.empty();
    }

    default Optional<String> processJsonMappingException(Throwable cause) {
        JsonMappingException jme = (JsonMappingException) cause;
        if (jme.getPath() != null && !jme.getPath().isEmpty()) {
            String fieldName = jme.getPath().stream()
                .map(ref -> ref.getFieldName() == null ? "[0]" : ref.getFieldName())
                .collect(Collectors.joining("."));
            return Optional.of("Invalid request field: "
                               + fieldName
                               + ": "
                               + jme.getOriginalMessage());
        }
        return Optional.empty();
    }

}
