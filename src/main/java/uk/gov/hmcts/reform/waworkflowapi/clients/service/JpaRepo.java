package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

@SuppressWarnings("unchecked")
@Component
public class JpaRepo {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Optional<IdempotentKeys> findById(IdempotentId idempotentId) {
        List<IdempotentKeys> result = em.createQuery("select i from IdempotentKeys i where i.idempotentId = ?1")
            .setParameter(1, idempotentId)
            .setHint("javax.persistence.lock.timeout", 0)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .getResultList();

        return Optional.ofNullable(result.get(0));
    }

}
