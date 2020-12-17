package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
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
}
