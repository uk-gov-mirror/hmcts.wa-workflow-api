package uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow;

import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class Transition {
    @ApiModelProperty(example = "caseCreated", required = true, notes = "The state the case was in before the event fired", position = 0)
    private final String startState;
    @ApiModelProperty(example = "submitCaseEvent", required = true, notes = "The event that triggered the transition", position = 1)
    private final String eventName;
    @ApiModelProperty(example = "caseSubmitted", required = true, notes = "The state the case was in after the event fired", position = 2)
    private final String endState;

    public Transition(String startState, String eventName, String endState) {
        this.startState = startState;
        this.eventName = eventName;
        this.endState = endState;
    }

    public String getStartState() {
        return startState;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEndState() {
        return endState;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Transition that = (Transition) object;
        return Objects.equals(startState, that.startState)
               && Objects.equals(eventName, that.eventName)
               && Objects.equals(endState, that.endState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startState, eventName, endState);
    }

    @Override
    public String toString() {
        return "Transition{"
               + "startState='" + startState + '\''
               + ", eventName='" + eventName + '\''
               + ", endState='" + endState + '\''
               + '}';
    }
}
