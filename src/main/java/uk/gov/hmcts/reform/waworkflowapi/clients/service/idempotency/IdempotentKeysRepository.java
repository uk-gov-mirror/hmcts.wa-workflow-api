package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.util.Optional;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

@Repository
@RepositoryRestResource(collectionResourceRel = "idempotentKeys", path = "idempotentKeys")
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    @NonNull
    @Transactional
    Optional<IdempotentKeys> findByIdempotencyKeyAndTenantId(@NonNull String idempotencyKey,
                                                             @NonNull String tenantId);

    @Override
    @RestResource(exported = false)
    @NonNull
    <S extends IdempotentKeys> S save(@NonNull S entity);

    @Override
    @RestResource(exported = false)
    @NonNull
    <S extends IdempotentKeys> Iterable<S> saveAll(@NonNull Iterable<S> entities);

    @Override
    @RestResource(exported = false)
    void deleteById(@NonNull IdempotentId idempotentId);

    @Override
    @RestResource(exported = false)
    void delete(@NonNull IdempotentKeys entity);

    @Override
    @RestResource(exported = false)
    void deleteAll(@NonNull Iterable<? extends IdempotentKeys> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();

}
