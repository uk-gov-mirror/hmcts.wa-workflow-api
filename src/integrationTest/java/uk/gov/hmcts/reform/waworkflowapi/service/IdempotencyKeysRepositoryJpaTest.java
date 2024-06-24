package uk.gov.hmcts.reform.waworkflowapi.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency.IdempotencyKeysRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("integration")
public class IdempotencyKeysRepositoryJpaTest {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private IdempotencyKeysRepository idempotencyKeysRepository;
    private IdempotencyKeys idempotencyKeys;


    @BeforeEach
    void setUp() {

        idempotencyKeys = new IdempotencyKeys(
            "some idempotency key",
            "some tenant id",
            "some process Id",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        entityManager.persist(idempotencyKeys);
    }

    @ParameterizedTest
    @CsvSource({
        "some idempotency key,some tenant id, true",
        "Some idempotency key,some tenant id, false",
        "Some idempotency key,Some tenant id, false",
    })
    public void givenIdempotencyKeysMatchingCase_then_expectedResult(String idempotencyKey,
                                                                     String tenantId,
                                                                     boolean result) {

        Optional<IdempotencyKeys> actual = idempotencyKeysRepository.findByIdempotencyKeyAndTenantId(
            idempotencyKey,
            tenantId
        );

        assertThat(actual.isPresent()).isEqualTo(result);
        actual.ifPresent(keys -> assertThat(keys).isEqualTo(idempotencyKeys));

    }
}
