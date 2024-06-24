package uk.gov.hmcts.reform.waworkflowapi.clients.service.idempotency;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotentId;

import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "idempotencyKeys", path = "idempotencyKeys")
public interface IdempotencyKeysRepository extends CrudRepository<IdempotencyKeys, IdempotentId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
    @NonNull
    @Transactional
    Optional<IdempotencyKeys> findByIdempotencyKeyAndTenantId(@NonNull String idempotencyKey,
                                                              @NonNull String tenantId);

    @Override
    @RestResource(exported = false)
    @NonNull
    <S extends IdempotencyKeys> S save(@NonNull S entity);

    @Override
    @RestResource(exported = false)
    @NonNull
    <S extends IdempotencyKeys> Iterable<S> saveAll(@NonNull Iterable<S> entities);

    @Override
    @RestResource(exported = false)
    void deleteById(@NonNull IdempotentId idempotentId);

    @Override
    @RestResource(exported = false)
    void delete(@NonNull IdempotencyKeys entity);

    @Override
    @RestResource(exported = false)
    void deleteAll(@NonNull Iterable<? extends IdempotencyKeys> entities);

    @Override
    @RestResource(exported = false)
    void deleteAll();

}
