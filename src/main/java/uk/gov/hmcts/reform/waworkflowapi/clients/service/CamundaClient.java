package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.AddProcessVariableRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaTask;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.EvaluateDmnRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.SendMessageRequest;
import uk.gov.hmcts.reform.waworkflowapi.config.CamundaFeignConfiguration;

import java.util.List;
import java.util.Map;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
@FeignClient(
    name = "camunda",
    url = "${camunda.url}",
    configuration = CamundaFeignConfiguration.class
)
public interface CamundaClient {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
        value = "/message",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                     SendMessageRequest sendMessageRequest);

    @PostMapping(
        value = "/decision-definition/key/{key}/tenant-id/{tenant-id}/evaluate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    List<Map<String, DmnValue<?>>> evaluateDmn(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("key") String key,
        @PathVariable("tenant-id") String tenantId,
        EvaluateDmnRequest evaluateDmnRequest
    );

    @PostMapping(value = "/process-instance",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<CamundaProcess> getProcessInstancesByVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestParam("variables") String variables,
        @RequestParam("activityIdIn") List<String> activityId
    );

    @GetMapping(value = "/process-instance/{key}/variables",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    CamundaProcessVariables getProcessInstanceVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("key") String processInstanceKey
    );

    @PostMapping(
        value = "/process-instance/{key}/variables",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    void updateProcessVariables(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("key") String key,
        AddProcessVariableRequest addProcessVariableRequest
    );

    @PostMapping(value = "/task",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    List<CamundaTask> searchByCaseId(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @RequestBody Map<String, Object> body);
}

