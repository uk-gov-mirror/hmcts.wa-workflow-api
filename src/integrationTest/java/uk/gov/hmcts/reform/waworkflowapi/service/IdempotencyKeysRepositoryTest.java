package uk.gov.hmcts.reform.waworkflowapi.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.ExternalTaskWorker;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyKeysRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
@Testcontainers
@ActiveProfiles("docker")
class IdempotencyKeysRepositoryTest {

    public static final String FAIL_TO_UPDATE_THIS = "fail to update this";
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:11"))
        .withDatabaseName("wa_workflow_api")
        .withUsername("postgres")
        .withPassword("pass");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.url",
            () -> String.format(
                "jdbc:postgresql://localhost:%d/%s",
                postgres.getFirstMappedPort(),
                postgres.getDatabaseName()
            )
        );
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }

    @Autowired
    private IdempotencyKeysRepository repository;
    private IdempotencyKeys idempotencyKeysWithRandomId;
    private IdempotentId randomIdempotentId;

    //because of the workers polling camunda at start-up
    @MockBean
    private ExternalTaskWorker externalTaskWorker;

    @BeforeEach
    void setUp() {
        randomIdempotentId = new IdempotentId(
            UUID.randomUUID().toString(),
            "ia"
        );

        idempotencyKeysWithRandomId = new IdempotencyKeys(
            randomIdempotentId.getIdempotencyKey(),
            randomIdempotentId.getTenantId(),
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void given_readQueryOnRow_then_anotherQueryOnSameRowThrowException() throws InterruptedException {
        log.info("executing test with idempotencyKeysWithRandomId({})", idempotencyKeysWithRandomId.toString());
        repository.save(idempotencyKeysWithRandomId);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(this::reader1);
        Future<?> futureException = executorService.submit(this::reader2);

        await()
            .ignoreException(AssertionError.class)
            .pollInterval(1, TimeUnit.SECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .until(() -> {
                Exception exception = Assertions.assertThrows(Exception.class, futureException::get);
                log.info(exception.toString());
                assertThat(exception).hasMessageContaining("PessimisticLockException");

                return true;
            });

        Optional<IdempotencyKeys> actual = repository.findByIdempotencyKeyAndTenantId(
            idempotencyKeysWithRandomId.getIdempotencyKey(),
            idempotencyKeysWithRandomId.getTenantId()
        );

        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getProcessId()).isNotEqualTo(FAIL_TO_UPDATE_THIS);

        executorService.shutdown();
        //noinspection ResultOfMethodCallIgnored
        executorService.awaitTermination(13, TimeUnit.SECONDS);
    }

    private void reader2() {
        // Allow some time to ensure the reader is executed first
        await().timeout(2, TimeUnit.SECONDS);
        log.info("start reader2...");
        repository.findByIdempotencyKeyAndTenantId(
            randomIdempotentId.getIdempotencyKey(),
            randomIdempotentId.getTenantId()
        );

        repository.save(new IdempotencyKeys(
            idempotencyKeysWithRandomId.getIdempotencyKey(),
            idempotencyKeysWithRandomId.getTenantId(),
            FAIL_TO_UPDATE_THIS,
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

    }

    private void reader1() {
        log.info("start reader1...");
        repository.findByIdempotencyKeyAndTenantId(
            randomIdempotentId.getIdempotencyKey(),
            randomIdempotentId.getTenantId()
        );
    }

}
