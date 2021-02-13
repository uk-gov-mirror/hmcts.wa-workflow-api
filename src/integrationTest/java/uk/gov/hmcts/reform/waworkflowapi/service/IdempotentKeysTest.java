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
import uk.gov.hmcts.reform.waworkflowapi.clients.service.JpaRepo;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.persistence.LockTimeoutException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("integration")
class IdempotentKeysTest {

    @Autowired
    private IdempotentKeysRepository repository;
    @Autowired
    private JpaRepo jpaRepo;
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
    void given_readQueryOnRow_then_anotherQueryOnSameRowThrowException() {
        repository.save(idempotentKeysWithRandomId);

        Thread query1 = new Thread(() -> {
            log.info("start query1-thread...");

            // set exclusive lock on row
            jpaRepo.findById(randomIdempotentId);

            // simulates isDuplicate checking takes some time
            Awaitility.await().timeout(15, TimeUnit.SECONDS);

        }, "query1-thread");

        query1.start();
        // Allow some time to ensure query1 is executed first
        Awaitility.await().timeout(2, TimeUnit.SECONDS);

        log.info("start query2...");

        try {
            jpaRepo.findById(randomIdempotentId);
        } catch (LockTimeoutException e) {
            Assertions.assertTrue(true);
            return;
        }

        Assertions.fail();

    }

}
