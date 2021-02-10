package uk.gov.hmcts.reform.waworkflowapi.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration")
class IdempotentKeysRepositoryTest {

    @Autowired
    private IdempotentKeysRepository repository;


    @Test
    public void simpleCrudExample() {

        String randomIdempotencyKey = UUID.randomUUID().toString();
        IdempotentId randomIdempotentId = new IdempotentId(
            randomIdempotencyKey,
            "ia"
        );

        IdempotentKeys idempotentKeyWithRandomId = new IdempotentKeys(
            randomIdempotentId,
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        //Create
        IdempotentKeys actualSavedEntity = repository.save(idempotentKeyWithRandomId);

        //Read
        Optional<IdempotentKeys> actualFoundEntity = repository.findById(randomIdempotentId);
        assertThat(actualFoundEntity.isPresent()).isTrue();
        assertThat(actualSavedEntity).isEqualTo(actualFoundEntity.get());

        //Update
        IdempotentKeys actualUpdatedEntity = repository.save(new IdempotentKeys(
            randomIdempotentId,
            "updated by me",
            LocalDateTime.now(),
            LocalDateTime.now()
        ));

        Optional<IdempotentKeys> actualFoundAndUpdatedEntity = repository.findById(randomIdempotentId);
        assertThat(actualFoundAndUpdatedEntity.isPresent()).isTrue();
        assertThat(actualUpdatedEntity).isEqualTo(actualFoundAndUpdatedEntity.get());
        assertThat(actualUpdatedEntity).isNotEqualTo(actualFoundEntity.get());

    }
}
