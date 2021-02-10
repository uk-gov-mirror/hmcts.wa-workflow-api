package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.util.Optional;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IdempotentKeys> findById(IdempotentId idempotentId);

}
