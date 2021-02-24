package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.WarningTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyTaskWorkerHandler;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("integration")
@Disabled("It is not stable enough. We want to keep it for when we have to test or debug DB lock scenarios locally.")
class IdempotentKeysRepositoryTest {

    public static final String EXPECTED_EXCEPTION = "org.springframework.orm.jpa.JpaSystemException";
    @Autowired
    private IdempotentKeysRepository repository;
    private IdempotentKeys idempotentKeysWithRandomId;
    private IdempotentId randomIdempotentId;

    //because of the workers polling camunda at start-up
    @MockBean
    private IdempotencyTaskWorkerHandler idempotencyTaskWorkerHandler;
    @MockBean
    private WarningTaskWorkerHandler warningTaskWorkerHandler;

    @BeforeEach
    void setUp() {
        randomIdempotentId = new IdempotentId(
            UUID.randomUUID().toString(),
            "ia"
        );

        idempotentKeysWithRandomId = new IdempotentKeys(
            randomIdempotentId.getIdempotencyKey(),
            randomIdempotentId.getTenantId(),
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void given_readQueryOnRow_then_anotherQueryOnSameRowThrowException() throws InterruptedException {
        repository.save(idempotentKeysWithRandomId);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(this::reader);
        Future<?> futureException = executorService.submit(this::writer);

        await()
            .ignoreExceptions()
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(15, TimeUnit.SECONDS)
            .until(() -> {

                ExecutionException exception = Assertions.assertThrows(ExecutionException.class, futureException::get);
                assertThat(exception.getMessage())
                    .startsWith(EXPECTED_EXCEPTION);

                return exception.getMessage().startsWith(EXPECTED_EXCEPTION);
            });

        executorService.shutdown();
        //noinspection ResultOfMethodCallIgnored
        executorService.awaitTermination(3, TimeUnit.MINUTES);
    }

    private void writer() {
        // Allow some time to ensure the reader is executed first
        await().timeout(2, TimeUnit.SECONDS);

        log.info("start read and write ops...");

        repository.findById(randomIdempotentId);
        repository.save(new IdempotentKeys(
            randomIdempotentId.getIdempotencyKey(),
            randomIdempotentId.getTenantId(),
            "should not update because of lock",
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

    }

    private void reader() {
        log.info("start reader thread...");
        repository.findById(randomIdempotentId);
    }

}
