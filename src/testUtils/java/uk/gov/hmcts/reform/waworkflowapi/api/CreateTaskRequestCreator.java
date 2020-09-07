package uk.gov.hmcts.reform.waworkflowapi.api;

import uk.gov.hmcts.reform.waworkflowapi.controllers.startworkflow.CreateTaskRequest;

import java.time.ZonedDateTime;

import static uk.gov.hmcts.reform.waworkflowapi.api.CreateTaskRequestBuilder.aCreateTaskRequest;
import static uk.gov.hmcts.reform.waworkflowapi.api.CreatorObjectMapper.asJsonString;
import static uk.gov.hmcts.reform.waworkflowapi.api.TransitionBuilder.aTransition;

public final class CreateTaskRequestCreator {
    private CreateTaskRequestCreator() {
    }

    public static CreateTaskRequest appealSubmittedCreateTaskRequest(String caseId) {
        return aCreateTaskRequest()
            .withCaseId(caseId)
            .withTransition(
                aTransition()
                    .withPreState("appealStarted")
                    .withEventId("submitAppeal")
                    .withPostState("appealSubmitted")
                    .build()
            )
            .build();
    }

    public static CreateTaskRequest appealSubmittedCreateTaskRequestWithDueDate(String caseId) {
        return aCreateTaskRequest()
            .withCaseId(caseId)
            .withTransition(
                aTransition()
                    .withPreState("appealStarted")
                    .withEventId("submitAppeal")
                    .withPostState("appealSubmitted")
                    .build()
            )
            .withDueDate(ZonedDateTime.now().plusDays(2))
            .build();
    }

    public static String appealSubmittedCreateTaskRequestString() {
        return asJsonString(appealSubmittedCreateTaskRequest("1234567890"));
    }

    public static CreateTaskRequest unmappedCreateTaskRequest(String caseId) {
        return aCreateTaskRequest()
            .withCaseId(caseId)
            .withTransition(
                aTransition()
                    .withPreState("appealStarted")
                    .withEventId("editAppeal")
                    .withPostState("appealStarted")
                    .build()
            )
            .withDueDate(ZonedDateTime.now().plusDays(2))
            .build();
    }

    public static String unmappedCreateTaskRequestString() {
        return asJsonString(unmappedCreateTaskRequest("1234567890"));
    }
}
