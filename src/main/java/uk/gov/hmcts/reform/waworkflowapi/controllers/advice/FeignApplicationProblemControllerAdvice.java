package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.spring.web.advice.validation.ValidationAdviceTrait;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;
import static org.zalando.problem.Status.UNAUTHORIZED;

@Slf4j
@ControllerAdvice(basePackages = {
    "uk.gov.hmcts.reform.waworkflowapi.controllers"
})
@RequestMapping(produces = APPLICATION_PROBLEM_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.DataflowAnomalyAnalysis",
    "PMD.UseStringBufferForStringAppends", "PMD.LawOfDemeter"})
public class FeignApplicationProblemControllerAdvice implements ValidationAdviceTrait,
    ApplicationProblemControllerAdviceBase {

    @ExceptionHandler(FeignException.ServiceUnavailable.class)
    public ResponseEntity<ThrowableProblem> handleFeignServiceUnavailableException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = SERVICE_UNAVAILABLE; //503
        return createDownStreamErrorResponse(statusType);
    }

    @ExceptionHandler(FeignException.BadGateway.class)
    public ResponseEntity<ThrowableProblem> handleFeignBadGatewayException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = BAD_GATEWAY; //502
        return createDownStreamErrorResponse(statusType);
    }

    @ExceptionHandler(FeignException.Forbidden.class)
    public ResponseEntity<ThrowableProblem> handleFeignForbiddenException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = FORBIDDEN; //403
        return createDownStreamErrorResponse(statusType);
    }

    @ExceptionHandler(FeignException.Unauthorized.class)
    public ResponseEntity<ThrowableProblem> handleFeignUnAuthorisedException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = UNAUTHORIZED; //401
        return createDownStreamErrorResponse(statusType);
    }

    @ExceptionHandler(FeignException.FeignServerException.class)
    public ResponseEntity<ThrowableProblem> handleFeignInternalServerErrorException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = INTERNAL_SERVER_ERROR; //500
        return createDownStreamErrorResponse(statusType);
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ThrowableProblem> handleFeignNotFoundException(FeignException ex) {
        log.error(EXCEPTION_OCCURRED, ex.getMessage(), ex);
        Status statusType = NOT_FOUND; //404
        return createDownStreamErrorResponse(statusType);
    }
}

