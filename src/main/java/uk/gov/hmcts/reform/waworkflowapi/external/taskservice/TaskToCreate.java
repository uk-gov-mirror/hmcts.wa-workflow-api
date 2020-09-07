package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import java.util.Objects;

public class TaskToCreate {
    private final Task task;
    private final String group;

    public TaskToCreate(Task task, String group) {
        this.task = task;
        this.group = group;
    }

    public Task getTask() {
        return task;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        TaskToCreate that = (TaskToCreate) object;
        return task == that.task
               && Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, group);
    }

    @Override
    public String toString() {
        return "TaskToCreate{"
               + "task=" + task
               + ", group='" + group + '\''
               + '}';
    }
}
