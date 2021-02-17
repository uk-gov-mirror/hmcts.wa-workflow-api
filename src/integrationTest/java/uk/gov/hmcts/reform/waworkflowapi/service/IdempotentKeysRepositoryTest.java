package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("integration")
class IdempotentKeysRepositoryTest {

    @Autowired
    private IdempotentKeysRepository repository;
    private IdempotentKeys idempotentKeysWithRandomId;
    private IdempotentId randomIdempotentId;

    @BeforeEach
    void setUp() {
        randomIdempotentId = new IdempotentId(
            UUID.randomUUID().toString(),
            "ia"
        );

        idempotentKeysWithRandomId = new IdempotentKeys(
            randomIdempotentId,
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

        ExecutionException exception = Assertions.assertThrows(ExecutionException.class, futureException::get);
        assertThat(exception.getMessage())
            .startsWith("org.springframework.dao.PessimisticLockingFailureException");

        executorService.shutdown();
        //noinspection ResultOfMethodCallIgnored
        executorService.awaitTermination(3, TimeUnit.MINUTES);
    }

    private void writer() {
        // Allow some time to ensure the reader is executed first
        Awaitility.await().timeout(2, TimeUnit.SECONDS);

        log.info("start read and write ops...");

        repository.findById(randomIdempotentId);
        repository.save(new IdempotentKeys(
            randomIdempotentId,
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
