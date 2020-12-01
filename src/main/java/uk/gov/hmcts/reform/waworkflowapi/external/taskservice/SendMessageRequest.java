package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class SendMessageRequest {
    private final String messageName;
    private final ProcessVariables processVariables;

    public SendMessageRequest(String messageName, ProcessVariables processVariables) {
        this.messageName = messageName;
        this.processVariables = processVariables;
    }

    public String getMessageName() {
        return messageName;
    }


    public ProcessVariables getProcessVariables() {
        return processVariables;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        SendMessageRequest that = (SendMessageRequest) object;
        return Objects.equals(messageName, that.messageName)
               && Objects.equals(processVariables, that.processVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageName, processVariables);
    }

    @Override
    public String toString() {
        return "SendMessageRequest{"
               + "messageName='" + messageName + '\''
               + ", processVariables=" + processVariables
               + '}';
    }
}
