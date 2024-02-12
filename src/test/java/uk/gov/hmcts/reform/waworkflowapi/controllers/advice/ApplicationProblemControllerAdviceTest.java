package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.collect.ImmutableList;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.GenericForbiddenException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.GenericServerErrorException;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.enums.ErrorMessages;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import javax.validation.ConstraintViolationException;

import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.BAD_REQUEST;
import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class ApplicationProblemControllerAdviceTest {

    private ApplicationProblemControllerAdvice applicationProblemControllerAdvice;
    private FeignApplicationProblemControllerAdvice feignApplicationProblemControllerAdvice;

    @BeforeEach
    void setUp() {
        applicationProblemControllerAdvice = new ApplicationProblemControllerAdvice();
        feignApplicationProblemControllerAdvice = new FeignApplicationProblemControllerAdvice();
    }

    @Test
    void should_handle_feign_bad_gateway_exception() {
        Request request = Request.create(Request.HttpMethod.GET, "url",
                                         new HashMap<>(), null, new RequestTemplate()
        );

        FeignException exception = new FeignException.BadGateway(
            "Bad Gateway",
            request,
            null,
            null
        );

        ResponseEntity<ThrowableProblem> response = feignApplicationProblemControllerAdvice
            .handleFeignBadGatewayException(exception);

        assertEquals(HttpStatus.BAD_GATEWAY.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/downstream-dependency-error"),
            response.getBody().getType()
        );
        assertEquals("Downstream Dependency Error", response.getBody().getTitle());
        assertEquals(ErrorMessages.DOWNSTREAM_DEPENDENCY_ERROR.getDetail(), response.getBody().getDetail());
        assertEquals(BAD_GATEWAY, response.getBody().getStatus());
    }

    @Test
    void should_handle_constraint_violation_exception() {
        NativeWebRequest nativeWebRequest = mock(NativeWebRequest.class);
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(emptySet());

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleConstraintViolation(constraintViolationException, nativeWebRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/constraint-validation"),
            response.getBody().getType()
        );
        assertEquals("Constraint Violation", response.getBody().getTitle());
        assertEquals(BAD_REQUEST, response.getBody().getStatus());
    }

    @Test
    void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException httpMessageNotReadableException =
            new HttpMessageNotReadableException("someMessage");

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleMessageNotReadable(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/bad-request"),
            response.getBody().getType()
        );
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("Invalid request message", response.getBody().getDetail());
        assertEquals(BAD_REQUEST, response.getBody().getStatus());
    }

    @Test
    void should_handle_http_message_not_readable_exception_json_parse() {
        HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        JsonParseException cause = mock(JsonParseException.class);
        when(cause.getOriginalMessage()).thenReturn("someMessage");
        when(httpMessageNotReadableException.getCause()).thenReturn(cause);

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleMessageNotReadable(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/bad-request"),
            response.getBody().getType()
        );
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("someMessage", response.getBody().getDetail());
        assertEquals(BAD_REQUEST, response.getBody().getStatus());
    }


    @Test
    void should_handle_http_message_not_readable_exception_mismatch_input() {
        HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        MismatchedInputException cause = mock(MismatchedInputException.class);
        List<JsonMappingException.Reference> paths = List.of(
            new JsonMappingException.Reference("someObject", "somefield"),
            new JsonMappingException.Reference("someObject", "someNestedFieldName")
        );
        when(cause.getPath()).thenReturn(paths);
        when(httpMessageNotReadableException.getCause()).thenReturn(cause);

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleMessageNotReadable(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/bad-request"),
            response.getBody().getType()
        );
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals("Invalid request field: somefield.someNestedFieldName", response.getBody().getDetail());
        assertEquals(BAD_REQUEST, response.getBody().getStatus());
    }

    @Test
    void should_handle_http_message_not_readable_exception_json_mapping() {
        HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        JsonMappingException cause = mock(JsonMappingException.class);
        when(cause.getOriginalMessage()).thenReturn("someMessage");
        List<JsonMappingException.Reference> paths = List.of(
            new JsonMappingException.Reference("someObject", "somefield"),
            new JsonMappingException.Reference("someObject", "someNestedFieldName")
        );
        when(cause.getPath()).thenReturn(paths);
        when(httpMessageNotReadableException.getCause()).thenReturn(cause);

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleMessageNotReadable(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(
            URI.create("https://github.com/hmcts/wa-workflow-api/problem/bad-request"),
            response.getBody().getType()
        );
        assertEquals("Bad Request", response.getBody().getTitle());
        assertEquals(
            "Invalid request field: somefield.someNestedFieldName: someMessage",
            response.getBody().getDetail()
        );
        assertEquals(BAD_REQUEST, response.getBody().getStatus());
    }

    @ParameterizedTest
    @MethodSource("exceptionDataProvider")
    void should_handle_exceptions_in_handleApplicationProblemExceptions(final GenericExceptionScenario expected) {

        ResponseEntity<Problem> response = applicationProblemControllerAdvice
            .handleApplicationProblemExceptions(expected.exception);

        assertEquals(expected.expectedStatus.getStatusCode(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expected.expectedType, response.getBody().getType());
        assertEquals(expected.expectedTitle, response.getBody().getTitle());
        assertEquals(expected.expectedStatus, response.getBody().getStatus());
    }

    private static List<GenericExceptionScenario> exceptionDataProvider() {

        return ImmutableList.of(
            GenericExceptionScenario.builder()
                .exception(new GenericForbiddenException(ErrorMessages.GENERIC_FORBIDDEN_ERROR))
                .expectedTitle("Forbidden")
                .expectedStatus(FORBIDDEN)
                .expectedType(URI.create("https://github.com/hmcts/wa-workflow-api/problem/forbidden"))
                .build(),

            GenericExceptionScenario.builder()
                .exception(new GenericServerErrorException(ErrorMessages.EVALUATE_DMN_ERROR))
                .expectedTitle("Generic Server Error")
                .expectedStatus(INTERNAL_SERVER_ERROR)
                .expectedType(URI.create("https://github.com/hmcts/wa-workflow-api/problem/generic-server-error"))
                .build()
        );
    }

    @Builder
    private static class GenericExceptionScenario {
        AbstractThrowableProblem exception;
        URI expectedType;
        String expectedTitle;
        Status expectedStatus;
    }


}
