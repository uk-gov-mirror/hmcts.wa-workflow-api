package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import javax.persistence.Embeddable;

@Embeddable
@EqualsAndHashCode
@ToString
public class IdempotentId implements Serializable {
    private String idempotencyKey;
    private String tenantId;

    public IdempotentId() {
    }

    public IdempotentId(String idempotencyKey, String tenantId) {
        this.idempotencyKey = idempotencyKey;
        this.tenantId = tenantId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getTenantId() {
        return tenantId;
    }
}
