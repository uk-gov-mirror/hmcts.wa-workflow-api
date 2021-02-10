package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.util.Optional;
import javax.persistence.LockModeType;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NonNull
    Optional<IdempotentKeys> findById(@NonNull IdempotentId idempotentId);

}
