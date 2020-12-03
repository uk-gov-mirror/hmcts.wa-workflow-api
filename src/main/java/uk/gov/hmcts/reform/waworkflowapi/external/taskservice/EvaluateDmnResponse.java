package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

public class EvaluateDmnResponse {
    @ApiModelProperty(example = "List[HashMap<String,DmnValue> }]", required = true, notes = "Service jurisdiction")
    private List<Map<String,Object>> results;

    public EvaluateDmnResponse() {
        // Empty constructor
    }

    public EvaluateDmnResponse(List<Map<String,Object>> results) {
        this.results = results;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }
}
