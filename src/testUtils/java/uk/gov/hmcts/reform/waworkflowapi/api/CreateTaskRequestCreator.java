package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;

import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestBuilder.aCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.api.TransitionBuilder.aTransition;

public final class CreateTaskRequestCreator {
    private CreateTaskRequestCreator() {
    }

    public static CreateTaskRequest appealSubmittedCreateTaskRequest() {
        return aCreateTaskRequest()
            .withCaseId("1234567890")
            .withTransition(
                aTransition()
                    .withStartState("appealCreated")
                    .withEventName("submitAppeal")
                    .withEndState("appealSubmitted")
                    .build()
            )
            .build();
    }

    public static String appealSubmittedCreateTaskRequestString() {
        return asJsonString(appealSubmittedCreateTaskRequest());
    }
}
