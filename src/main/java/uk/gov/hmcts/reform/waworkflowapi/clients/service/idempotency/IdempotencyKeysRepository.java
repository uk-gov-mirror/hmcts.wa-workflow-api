package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;

import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

@Repository
public interface IdempotencyKeysRepository extends CrudRepository<IdempotencyKeys, IdempotentId> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    @NonNull
    @Transactional
    Optional<IdempotencyKeys> findById(@NonNull IdempotentId idempotentId);

}
