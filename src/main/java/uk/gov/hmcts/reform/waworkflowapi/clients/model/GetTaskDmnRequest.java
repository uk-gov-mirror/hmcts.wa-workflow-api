package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }
        GetTaskDmnRequest that = (GetTaskDmnRequest) anotherObject;
        return Objects.equals(eventId, that.eventId)
               && Objects.equals(postEventState, that.postEventState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, postEventState);
    }

    @Override
    public String toString() {
        return "GetTaskDmnRequest{"
               + "eventId=" + eventId
               + ", postEventState=" + postEventState
               + '}';
    }
}
