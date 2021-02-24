package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ExternalTaskWorkerTest {

    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler;
    @Mock
    WarningTaskWorkerHandler warningTaskWorkerHandler;

    ExternalTaskWorker externalTaskWorker;

    @BeforeEach
    void setUp() {
        externalTaskWorker = new ExternalTaskWorker(
            "some camunda url",
            authTokenGenerator,
            idempotencyTaskWorkerHandler,
            warningTaskWorkerHandler
        );
    }

    @Test
    void setupClient() {
        assertThat(ReflectionTestUtils.getField(externalTaskWorker, "authTokenGenerator")).isNotNull();
        assertThat(ReflectionTestUtils.getField(externalTaskWorker, "idempotencyTaskWorkerHandler")).isNotNull();
        assertThat(ReflectionTestUtils.getField(externalTaskWorker, "warningTaskWorkerHandler")).isNotNull();
        assertThat(ReflectionTestUtils.getField(externalTaskWorker, "camundaUrl")).isEqualTo("some camunda url");
    }

}
