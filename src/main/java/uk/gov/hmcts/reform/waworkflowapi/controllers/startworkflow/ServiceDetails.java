package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class ServiceDetails {
    @Schema(
        example = "IA",
        required = true,
        description = "Service jurisdiction"
    )
    private final String jurisdiction;
    @Schema(
        example = "Asylum",
        required = true,
        description = "Service case type"
    )
    private final String caseType;

    public ServiceDetails(String jurisdiction, String caseType) {
        this.jurisdiction = jurisdiction;
        this.caseType = caseType;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getCaseType() {
        return caseType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ServiceDetails that = (ServiceDetails) object;
        return Objects.equals(jurisdiction, that.jurisdiction)
               && Objects.equals(caseType, that.caseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jurisdiction, caseType);
    }

    @Override
    public String toString() {
        return "ServiceDetails{"
               + "jurisdiction='" + jurisdiction + '\''
               + ", caseType='" + caseType + '\''
               + '}';
    }
}
