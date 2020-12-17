package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@ToString
public class EvaluateDmnResponse {
    @ApiModelProperty(example = "List[HashMap<String,DmnValue> }]", required = true, notes = "Service jurisdiction")
    private List<Map<String, DmnValue<?>>> results;

    public EvaluateDmnResponse() {
        // Empty constructor
    }

    public EvaluateDmnResponse(List<Map<String, DmnValue<?>>> results) {
        this.results = results;
    }

    public List<Map<String, DmnValue<?>>> getResults() {
        return results;
    }
}
