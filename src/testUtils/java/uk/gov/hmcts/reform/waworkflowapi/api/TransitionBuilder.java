package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.Transition;

public final class TransitionBuilder {
    private String preState;
    private String eventId;
    private String postState;

    private TransitionBuilder() {
    }

    public static TransitionBuilder aTransition() {
        return new TransitionBuilder();
    }

    public TransitionBuilder withPreState(String preState) {
        this.preState = preState;
        return this;
    }

    public TransitionBuilder withEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public TransitionBuilder withPostState(String postState) {
        this.postState = postState;
        return this;
    }

    public Transition build() {
        return new Transition(preState, eventId, postState);
    }
}
