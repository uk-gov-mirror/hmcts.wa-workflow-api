package uk.gov.hmcts.reform.waworkflowapi.common;

import java.util.Objects;

public class TaskToCreate {
    private final String task;
    private final String group;
    private final int workingDaysAllowed;
    private final String name;

    public TaskToCreate(String task, String group, int workingDaysAllowed, String name) {
        this.task = task;
        this.group = group;
        this.workingDaysAllowed = workingDaysAllowed;
        this.name = name;
    }

    public TaskToCreate(String task, String group, String name) {
        this(task, group, 0, name);
    }

    public String getTask() {
        return task;
    }

    public String getGroup() {
        return group;
    }

    public int getWorkingDaysAllowed() {
        return workingDaysAllowed;
    }

    public String getName() {
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
        TaskToCreate that = (TaskToCreate) object;
        return Objects.equals(task, that.task)
               && Objects.equals(group, that.group)
               && Objects.equals(workingDaysAllowed, that.workingDaysAllowed)
               && Objects.equals(name, that.name);
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
               + ", workingDaysAllowed='" + workingDaysAllowed + '\''
               + ", name='" + name + '\''
               + '}';
    }
}
