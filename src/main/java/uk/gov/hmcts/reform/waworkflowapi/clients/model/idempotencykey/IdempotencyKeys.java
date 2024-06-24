package uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@ToString
@EqualsAndHashCode
@IdClass(IdempotentId.class)
public class IdempotencyKeys implements Serializable {

    private static final long serialVersionUID = -7833073837420947484L;

    @Id
    private String idempotencyKey;
    @Id
    private String tenantId;
    private String processId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public IdempotencyKeys() {
        //needed for spring boot data JPA
    }

    public IdempotencyKeys(String idempotencyKey,
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
