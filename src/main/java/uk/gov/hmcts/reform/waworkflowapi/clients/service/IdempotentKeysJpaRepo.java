package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Repository
@Transactional
public class IdempotentKeysJpaRepo {

    @PersistenceContext
    private EntityManager entityManager;

    public IdempotentKeys findById(IdempotentId idempotentId) {

        Query query = entityManager.createQuery("select item from IdempotentKeys item where item.idempotentId = :id");

        query.setParameter("id", idempotentId);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        return (IdempotentKeys) query.getResultList().get(0);

    }

}
