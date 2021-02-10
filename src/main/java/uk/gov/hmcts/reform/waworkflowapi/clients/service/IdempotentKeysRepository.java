package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentKeys;

import java.util.Optional;
import javax.persistence.LockModeType;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public Optional<IdempotentKeys> findById(IdempotentId idempotentId);

}
