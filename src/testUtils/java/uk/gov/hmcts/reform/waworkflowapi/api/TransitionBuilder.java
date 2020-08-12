package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

public final class TransitionBuilder {
    private String startState;
    private String eventName;
    private String endState;

    private TransitionBuilder() {
    }

    public static TransitionBuilder aTransition() {
        return new TransitionBuilder();
    }

    public TransitionBuilder withStartState(String startState) {
        this.startState = startState;
        return this;
    }

    public TransitionBuilder withEventName(String eventName) {
        this.eventName = eventName;
        return this;
    }

    public TransitionBuilder withEndState(String endState) {
        this.endState = endState;
        return this;
    }

    public Transition build() {
        return new Transition(startState, eventName, endState);
    }
}
