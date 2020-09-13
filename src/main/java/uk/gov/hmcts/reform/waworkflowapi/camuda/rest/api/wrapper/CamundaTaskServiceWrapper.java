package uk.gov.hmcts.reform.waworkflowapi.camuda.rest.api.wrapper;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(
    name = "tasks",
    url = "${camunda.url}"
)
@Service
public interface CamundaTaskServiceWrapper {

    @GetMapping(value = "/task/{task-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody String getTask(@PathVariable("task-id") String id);
}
