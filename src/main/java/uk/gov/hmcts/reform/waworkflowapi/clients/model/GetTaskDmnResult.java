package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import java.util.Objects;

public class GetTaskDmnResult {
    private DmnValue<String> taskId;
    private DmnValue<String> group;
    private DmnValue<Integer> workingDaysAllowed;
    private DmnValue<String> name;

    private GetTaskDmnResult() {
    }

    public GetTaskDmnResult(DmnValue<String> taskId, DmnValue<String> group, DmnValue<Integer> workingDaysAllowed, DmnValue<String> name) {
        this.taskId = taskId;
        this.group = group;
        this.workingDaysAllowed = workingDaysAllowed;
        this.name = name;
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

    public DmnValue<String> getName() {
        return name;
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
               && Objects.equals(workingDaysAllowed, that.workingDaysAllowed)
               && Objects.equals(name, that.name);
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
               + ", name=" + name
               + '}';
    }
}
