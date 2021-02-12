package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys2;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysJpaRepo;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.JdbcRepo;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
//@ActiveProfiles("integration")
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
    void jpaTest() {
        repository.save(idempotentKeysWithRandomId);

        Thread user1 = new Thread(() -> {
            log.info("start user1-thread...");
            repository.findById(randomIdempotentId);
            Awaitility.await().timeout(15, TimeUnit.SECONDS);
            repository.save(new IdempotentKeys(
                randomIdempotentId,
                "updated",
                LocalDateTime.now(),
                LocalDateTime.now()
            ));
        }, "user1-thread");

        Thread user2 = new Thread(() -> {
            log.info("start user2-thread...");
            repository.findById(randomIdempotentId);
            repository.save(new IdempotentKeys(
                randomIdempotentId,
                "updated2",
                LocalDateTime.now(),
                LocalDateTime.now()
            ));
        }, "user2-thread");

        user1.start();
        Awaitility.await().timeout(2, TimeUnit.SECONDS);
        user2.start();

    }

    @Test
    void givenQueriesForDifferentIdsThenTheyAreNotBlocked() {
        log.info("delete all");
        repository.deleteAll();

        IdempotentId idempotentId1 = new IdempotentId("1", "ia");
        IdempotentKeys idempotentKeys1 = new IdempotentKeys(
            idempotentId1,
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        log.info("writer");
        writer(idempotentKeys1, 0);

        new Thread(() -> runReader(idempotentId1, 4), "readerThread").start();
        new Thread(() -> writer(new IdempotentKeys(
            idempotentId1,
            "updated",
            LocalDateTime.now(),
            LocalDateTime.now()
        ), 2), "writerThread").start();

        Awaitility.await().atMost(5, TimeUnit.SECONDS);
    }

    private void runReader(IdempotentId idempotentId1, int timeout) {
        log.info("begin reader ({})", idempotentId1);
        log.info("begin reader ({}) before findById...", idempotentId1);
        IdempotentKeys actualIdempotentKeys1 = idempotentKeysJpaRepo.findById(idempotentId1);
        log.info("begin reader ({}) after findById...", idempotentId1);
//        assertThat(actualIdempotentKeys1.isPresent()).isTrue();
        log.info("begin reader (result:{}) ...", actualIdempotentKeys1.toString());
        log.info("begin reader ({}) waiting...", idempotentId1);
        Awaitility.await().atMost(timeout, TimeUnit.SECONDS);
        log.info("end reader ({})", idempotentId1);
    }

    private void writer(IdempotentKeys idempotentKeys1, int timeout) {
        log.info("begin writer ({})", idempotentKeys1);
        Awaitility.await().atMost(timeout, TimeUnit.SECONDS);
        repository.save(idempotentKeys1);
        log.info("end writer ({})", idempotentKeys1);
    }

}
