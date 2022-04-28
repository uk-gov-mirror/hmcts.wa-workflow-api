package uk.gov.hmcts.reform.waworkflowapi.entities;

import io.restassured.http.Header;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SpecificStandaloneRequest {

    private Header authenticationHeaders;
    private String jurisdiction;
    private String caseType;
    private String taskType;
    private String taskName;
    private String roleCategory;
    private String caseId;

}
