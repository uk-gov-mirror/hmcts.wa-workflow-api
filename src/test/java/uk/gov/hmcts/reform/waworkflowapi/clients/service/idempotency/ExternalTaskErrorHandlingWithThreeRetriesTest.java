package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.exceptions.IdempotencyTaskWorkerException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalTaskErrorHandlingWithThreeRetriesTest {

    @Mock
    private ExternalTask externalTask;
    @Mock
    private ExternalTaskService externalTaskService;
    @Mock
    private Exception exception;

    private final ExternalTaskErrorHandlingWithThreeRetries externalTaskErrorHandlingWithThreeRetries =
        new ExternalTaskErrorHandlingWithThreeRetries();

    @BeforeEach
    void setUp() {
        when(externalTask.getId()).thenReturn("some external task id");
        when(exception.toString()).thenReturn("some exception");
        when(exception.getMessage()).thenReturn("some exception details message");
    }

    @Test
    void given_an_error_then_set_handle_failure_with_three_retries() {
        given(externalTask.getRetries()).willReturn(null);

        externalTaskErrorHandlingWithThreeRetries.handleError(externalTask, externalTaskService, exception);

        verify(externalTaskService).handleFailure(
            "some external task id",
            "some exception",
            "some exception details message",
            3,
            1000
        );
    }

    @Test
    void given_another_error_and_given_retries_limit_has_not_reached_then_set_handle_failure_and_reduce_retries() {
        given(externalTask.getRetries()).willReturn(3);

        externalTaskErrorHandlingWithThreeRetries.handleError(externalTask, externalTaskService, exception);

        verify(externalTaskService).handleFailure(
            "some external task id",
            "some exception",
            "some exception details message",
            2,
            1000
        );
    }

    @Test
    void given_retries_limit_is_reached_then_set_handle_failure_with_zero_retries_and_throw_exception() {
        given(externalTask.getRetries()).willReturn(1);

        IdempotencyTaskWorkerException actualException = Assertions.assertThrows(
            IdempotencyTaskWorkerException.class,
            () -> externalTaskErrorHandlingWithThreeRetries.handleError(externalTask, externalTaskService, exception)
        );

        assertThat(actualException.getMessage()).isEqualTo(String.format(
            "Retrying three times did not fix the problem.%n"
                + "This external task(%s) failure causes an incident(%s).",
            externalTask.getId(),
            externalTask.getProcessInstanceId()
        ));

        verify(externalTaskService).handleFailure(
            "some external task id",
            "some exception",
            "some exception details message",
            0,
            1000
        );
    }

}
