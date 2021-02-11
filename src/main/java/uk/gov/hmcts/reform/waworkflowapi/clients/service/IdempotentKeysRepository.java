package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKey;

import java.util.Optional;
import javax.persistence.LockModeType;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKey, IdempotentId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @NonNull
    @Transactional
    Optional<IdempotentKey> findById(@NonNull IdempotentId idempotentId);

}
