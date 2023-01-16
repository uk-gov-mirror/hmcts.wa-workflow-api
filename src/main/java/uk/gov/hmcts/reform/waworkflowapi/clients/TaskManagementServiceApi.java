package uk.gov.hmcts.reform.waworkflowapi.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.waworkflowapi.config.FeignClientSnakeCaseConfiguration;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "wa-task-management-api",
    url = "${wa-task-management-api.url}",
    configuration = FeignClientSnakeCaseConfiguration.class
)
public interface TaskManagementServiceApi {

    @PostMapping(
        value = "/task/{task-id}/notes",
        consumes = APPLICATION_JSON_VALUE
    )
    void addTaskNote(@RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
                     @PathVariable("task-id") String taskId,
                     @RequestBody NotesRequest body);
}
