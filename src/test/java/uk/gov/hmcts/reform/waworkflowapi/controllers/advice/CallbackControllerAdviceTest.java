package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerAdviceTest {

    private final CallbackControllerAdvice callbackControllerAdvice = new CallbackControllerAdvice();

    @Test
    public void should_handle_generic_exception() {

        final String exceptionMessage = "Some exception message";
        final Exception exception = new Exception(exceptionMessage);

        ResponseEntity<String> response = callbackControllerAdvice.handleGenericException(exception);

        assertEquals(response.getStatusCode().value(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertEquals(response.getBody(), exceptionMessage);
    }
}
