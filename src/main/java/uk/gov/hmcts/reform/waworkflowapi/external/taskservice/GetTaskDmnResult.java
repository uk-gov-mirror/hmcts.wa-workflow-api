package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class GetTaskDmnResult {
    private DmnValue<String> taskId;
    private DmnValue<String> group;
    private DmnValue<Integer> workingDaysAllowed;

    private GetTaskDmnResult() {
    }

    public GetTaskDmnResult(DmnValue<String> taskId, DmnValue<String> group, DmnValue<Integer> workingDaysAllowed) {
        this.taskId = taskId;
        this.group = group;
        this.workingDaysAllowed = workingDaysAllowed;
    }

    public DmnValue<String> getTaskId() {
        return taskId;
    }

    public DmnValue<String> getGroup() {
        return group;
    }

    public DmnValue<Integer> getWorkingDaysAllowed() {
        return workingDaysAllowed;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        GetTaskDmnResult that = (GetTaskDmnResult) object;
        return Objects.equals(taskId, that.taskId)
               && Objects.equals(group, that.group)
               && Objects.equals(workingDaysAllowed, that.workingDaysAllowed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "GetTaskDmnResult{"
               + "taskId=" + taskId
               + ", group=" + group
               + ", workingDaysAllowed=" + workingDaysAllowed
               + '}';
    }
}
