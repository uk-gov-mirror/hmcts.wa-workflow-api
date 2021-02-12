package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys2;

import java.util.Optional;

@Repository
public class JdbcRepo {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Optional<IdempotentKeys2> findById(IdempotentId id) {
        return jdbcTemplate.queryForObject(
            "select * from wa_workflow_api.idempotent_keys where idempotency_key = ? and tenant_id = ?  for update",
            new Object[]{id.getIdempotencyKey(), id.getTenantId()},
            (rs, rowNum) ->
                Optional.of(new IdempotentKeys2(
                    rs.getString("idempotency_key"),
                    rs.getString("tenant_id"),
                    rs.getString("process_id"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("last_updated_at")
                ))
        );
    }
}
