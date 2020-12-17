package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class GetTaskDmnRequest {
    private final DmnValue eventId;
    private final DmnValue postEventState;

    public GetTaskDmnRequest(DmnValue eventId, DmnValue postEventState) {
        this.eventId = eventId;
        this.postEventState = postEventState;
    }

    public DmnValue getEventId() {
        return eventId;
    }

    public DmnValue getPostEventState() {
        return postEventState;
    }
}
