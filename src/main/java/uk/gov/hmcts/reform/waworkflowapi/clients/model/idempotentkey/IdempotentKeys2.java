package uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.sql.Timestamp;

@ToString
@EqualsAndHashCode
public class IdempotentKeys2 {

    private String idempotencyKey;
    private String tenantId;
    private String processId;
    private Timestamp createdAt;
    private Timestamp lastUpdatedAt;

    public IdempotentKeys2(String idempotencyKey,
                           String tenantId,
                           String processId,
                           Timestamp createdAt,
                           Timestamp lastUpdatedAt) {
        this.idempotencyKey = idempotencyKey;
        this.tenantId = tenantId;
        this.processId = processId;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }


    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getProcessId() {
        return processId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
