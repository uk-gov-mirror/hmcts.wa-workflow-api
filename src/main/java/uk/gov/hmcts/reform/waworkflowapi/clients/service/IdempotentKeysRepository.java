package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentId;

import java.util.Optional;
import javax.persistence.LockModeType;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IdempotentKeys> findById(IdempotentId idempotentId);

}
