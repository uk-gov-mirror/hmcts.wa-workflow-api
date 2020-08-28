package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class GetTaskDmnResult {
    private DmnValue taskId;

    private GetTaskDmnResult() {
    }

    public GetTaskDmnResult(DmnValue taskId) {
        this.taskId = taskId;
    }

    public DmnValue getTaskId() {
        return taskId;
    }

    @Override
    public boolean equals(Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }
        GetTaskDmnResult that = (GetTaskDmnResult) anotherObject;
        return Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "GetTaskDmnResult{"
               + "taskId=" + taskId
               + '}';
    }
}
