package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.ToString;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@ToString
public class IdempotentKeys {

    @Id
    private String idempotencyKey;
    private String tenantId;
    private String processId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public IdempotentKeys() {
    }

    public IdempotentKeys(String idempotencyKey,
                          String tenantId,
                          String processId,
                          LocalDateTime createdAt,
                          LocalDateTime lastUpdatedAt) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
}
