package uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@ToString
@EqualsAndHashCode
@IdClass(IdempotentId.class)
public class IdempotentKeys implements Serializable {

    private static final long serialVersionUID = -5290405936923531975L;
    @Id
    private String idempotencyKey;
    @Id
    private String tenantId;
    private String processId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

    public IdempotentKeys() {
        //needed for spring boot data JPA
    }

    public IdempotentKeys(String processId,
                          LocalDateTime createdAt,
                          LocalDateTime lastUpdatedAt) {
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
