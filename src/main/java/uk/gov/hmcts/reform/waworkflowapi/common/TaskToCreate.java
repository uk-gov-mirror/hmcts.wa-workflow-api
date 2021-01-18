package uk.gov.hmcts.reform.waworkflowapi.common;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class TaskToCreate {
    private final String task;
    private final String group;
    private final int workingDaysAllowed;
    private final String name;
    private final String delayUntil;

    public TaskToCreate(String task, String group, int workingDaysAllowed,
                        String name, String delayUntil) {
        this.task = task;
        this.group = group;
        this.workingDaysAllowed = workingDaysAllowed;
        this.name = name;
        this.delayUntil = delayUntil;

    }

    public TaskToCreate(String task, String group,
                        String name, String delayUntil) {
        this(task, group, 0, name, delayUntil);
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

    public String getDelayUntil() {
        return delayUntil;
    }

}
