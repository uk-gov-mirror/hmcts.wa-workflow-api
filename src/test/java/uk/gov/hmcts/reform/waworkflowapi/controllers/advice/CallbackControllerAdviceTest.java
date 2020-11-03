package uk.gov.hmcts.reform.waworkflowapi.controllers.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerAdviceTest {

    private final CallbackControllerAdvice callbackControllerAdvice = new CallbackControllerAdvice();

    @Test
    public void should_handle_generic_exception() {

        final String exceptionMessage = "Some exception message";
        final Exception exception = new Exception(exceptionMessage);

        ResponseEntity<ErrorMessage> response = callbackControllerAdvice.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(Timestamp.valueOf(LocalDateTime.now()).before(response.getBody().getTimestamp()));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), response.getBody().getError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals(exceptionMessage, response.getBody().getMessage());
    }
}
