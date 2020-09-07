package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;

public class ProcessVariables {
    private final DmnValue ccdId;
    private final DmnValue task;
    private final DmnValue group;
    private final DmnValue dueDate;

    public ProcessVariables(String ccdId, Task task, String group, String dueDate) {
        this.ccdId = dmnStringValue(ccdId);
        this.task = dmnStringValue(task.getId());
        this.group = dmnStringValue(group);
        this.dueDate = dmnStringValue(dueDate);
    }

    public DmnValue getCcdId() {
        return ccdId;
    }

    public DmnValue getTask() {
        return task;
    }

    public DmnValue getGroup() {
        return group;
    }

    public DmnValue getDueDate() {
        return dueDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ProcessVariables that = (ProcessVariables) object;
        return Objects.equals(ccdId, that.ccdId)
               && Objects.equals(task, that.task)
               && Objects.equals(group, that.group)
               && Objects.equals(dueDate, that.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccdId, task);
    }

    @Override
    public String toString() {
        return "ProcessVariables{"
               + "ccdId=" + ccdId
               + ", task=" + task
               + ", group=" + group
               + ", dueDate=" + dueDate
               + '}';
    }
}
