package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(
    name = "camunda",
    url = "${camunda.url}"
)
public interface CamundaClient {
    @PostMapping(value = "/decision-definition/key/getTask/evaluate", produces = MediaType.APPLICATION_JSON_VALUE)
    List<GetTaskDmnResult> getTask(DmnRequest<GetTaskDmnRequest> requestParameters);

    @PostMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
    void sendMessage(SendMessageRequest sendMessageRequest);
}
