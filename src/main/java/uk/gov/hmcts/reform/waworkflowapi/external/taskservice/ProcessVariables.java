package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static uk.gov.hmcts.reform.waworkflowapi.external.taskservice.DmnValue.dmnStringValue;

public class ProcessVariables {
    private final DmnValue<String> ccdId;
    private final DmnValue<String> taskId;
    private final DmnValue<String> group;
    private final DmnValue<String> dueDate;

    public ProcessVariables(String ccdId, Task taskId, String group, ZonedDateTime dueDate) {
        this.ccdId = dmnStringValue(ccdId);
        this.taskId = dmnStringValue(taskId.getId());
        this.group = dmnStringValue(group);
        this.dueDate = dmnStringValue(dueDate.format(DateTimeFormatter.ISO_INSTANT));
    }

    public DmnValue<String> getCcdId() {
        return ccdId;
    }

    public DmnValue<String> getTaskId() {
        return taskId;
    }

    public DmnValue<String> getGroup() {
        return group;
    }

    public DmnValue<String> getDueDate() {
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
               && Objects.equals(taskId, that.taskId)
               && Objects.equals(group, that.group)
               && Objects.equals(dueDate, that.dueDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ccdId, taskId);
    }

    @Override
    public String toString() {
        return "ProcessVariables{"
               + "ccdId=" + ccdId
               + ", taskId=" + taskId
               + ", group=" + group
               + ", dueDate=" + dueDate
               + '}';
    }
}
