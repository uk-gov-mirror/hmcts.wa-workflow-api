package uk.gov.hmcts.reform.waworkflowapi.clients.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityInstance {

    private String activityId;
    private String activityType;

    public ActivityInstance() {
        //Hidden constructor
        super();
    }

    public ActivityInstance(String activityId, String activityType) {
        this.activityId = activityId;
        this.activityType = activityType;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getActivityType() {
        return activityType;
    }
}
