package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.IdempotentKeys;

@Repository
public interface IdempotentKeysRepository extends CrudRepository<IdempotentKeys, IdempotentId> {

}
