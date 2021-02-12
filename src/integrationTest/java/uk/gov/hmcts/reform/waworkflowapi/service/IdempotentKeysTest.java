package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysJpaRepo;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.JdbcRepo;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class IdempotentKeysTest {

    @Autowired
    private IdempotentKeysRepository repository;
    private IdempotentKeys idempotentKeysWithRandomId;
    private IdempotentId randomIdempotentId;
    @Autowired
    private IdempotentKeysJpaRepo idempotentKeysJpaRepo;
    @Autowired
    private JdbcRepo jdbcRepo;

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
    @Ignore(value = "this test is supposed to run on the minikube local env to check the lock mechanism in postgres db")
    void given_readQueryOnRow_then_anotherQueryOnSameRowThrowException() {
        repository.save(idempotentKeysWithRandomId);

        Thread query1 = new Thread(() -> {
            log.info("start query1-thread...");

            // set exclusive lock on row
            repository.findById(randomIdempotentId);

            // simulates isDuplicate processing time
            Awaitility.await().timeout(15, TimeUnit.SECONDS);

            // finally update the row
            repository.save(new IdempotentKeys(
                randomIdempotentId,
                "updated",
                LocalDateTime.now(),
                LocalDateTime.now()
            ));

        }, "query1-thread");

        query1.start();
        // Allow some time to ensure query1 is executed first
        Awaitility.await().timeout(2, TimeUnit.SECONDS);

        log.info("start query2...");
        Assertions.assertThrows(
            PessimisticLockingFailureException.class,
            () -> repository.findById(randomIdempotentId)
        );

    }

}
