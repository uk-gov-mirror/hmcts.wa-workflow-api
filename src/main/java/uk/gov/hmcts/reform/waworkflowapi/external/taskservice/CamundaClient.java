package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
        value = "/decision-definition/key/{decisionTableName}-{jurisdiction}-{caseType}/evaluate",
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<GetTaskDmnResult> evaluateDmnTable(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("decisionTableName") String decisionTableName,
        @PathVariable("jurisdiction") String jurisdiction,
        @PathVariable("caseType") String caseType,
        DmnRequest<GetTaskDmnRequest> requestParameters
    );

    @PostMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                     SendMessageRequest sendMessageRequest);
}
