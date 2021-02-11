package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKey;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@ActiveProfiles("integration")
class IdempotentKeyTest {

    @Autowired
    private IdempotentKeysRepository repository;
    private uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKey idempotentKeyWithRandomId;
    private IdempotentId randomIdempotentId;

    @BeforeEach
    void setUp() {
        randomIdempotentId = new IdempotentId(
            UUID.randomUUID().toString(),
            "ia"
        );

        idempotentKeyWithRandomId = new uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKey(
            randomIdempotentId,
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void givenRecordWithPessimisticReadQuery_WhenQueryingNewOne_PessimisticLockExceptionThrown() {

        repository.save(idempotentKeyWithRandomId);

        Runnable queryTask = () -> {
            try {
                String threadName = Thread.currentThread().getName();
                log.info("begin with ThreadName: " + threadName);

                repository.findById(randomIdempotentId);

                TimeUnit.SECONDS.sleep(5);

                IdempotentKey updatedIdempotentKey = repository.save(new IdempotentKey(
                    randomIdempotentId,
                    "updated by thread: " + threadName,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                ));
                log.info("updatedIdempotentKey: " + updatedIdempotentKey);

                log.info("end with ThreadName: " + threadName);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(queryTask);
        executor.submit(queryTask);

        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            executor.shutdownNow();
            System.out.println("shutdown finished");
        }

        log.info("Done!");

    }

    @Test
    public void simpleCrudExample() {

        //Create
         IdempotentKey actualSavedEntity = repository.save(idempotentKeyWithRandomId);

        //Read
        Optional<IdempotentKey> actualFoundEntity = repository.findById(randomIdempotentId);
        assertThat(actualFoundEntity.isPresent()).isTrue();
        assertThat(actualSavedEntity).isEqualTo(actualFoundEntity.get());

        //Update
        IdempotentKey actualUpdatedEntity = repository.save(new IdempotentKey(
            randomIdempotentId,
            "updated by me",
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        Optional<IdempotentKey> actualFoundAndUpdatedEntity = repository.findById(randomIdempotentId);
        assertThat(actualFoundAndUpdatedEntity.isPresent()).isTrue();
        assertThat(actualUpdatedEntity).isEqualTo(actualFoundAndUpdatedEntity.get());
        assertThat(actualUpdatedEntity).isNotEqualTo(actualFoundEntity.get());

    }
}
