package uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@ToString
@EqualsAndHashCode
public class IdempotencyKeys {

    @EmbeddedId
    private IdempotentId idempotentId;
    private String processId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public IdempotencyKeys() {
        //needed for spring boot data JPA
    }

    public IdempotencyKeys(IdempotentId idempotentId,
                           String processId,
                           LocalDateTime createdAt,
                           LocalDateTime lastUpdatedAt) {
        this.idempotentId = idempotentId;
        this.processId = processId;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public IdempotentId getIdempotentId() {
        return idempotentId;
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
