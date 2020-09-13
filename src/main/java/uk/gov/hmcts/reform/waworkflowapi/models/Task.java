package uk.gov.hmcts.reform.waworkflowapi.models;

import java.util.Objects;

@SuppressWarnings({"PMD.TooManyFields", "PMD.ShortClassName", "PMD.UnnecessaryConstructor",
    "PMD.UncommentedEmptyConstructor","PMD.ExcessivePublicCount"})
public class Task {

    private String id;
    private String name;
    private String assignee;
    private String created;
    private String due;
    private String followUp;
    private String delegationState;
    private String description;
    private String executionId;
    private String owner;
    private String parentTaskId;
    private int priority;
    private String processDefinitionId;
    private String processInstanceId;
    private String taskDefinitionKey;
    private String caseExecutionId;
    private String caseInstanceId;
    private String caseDefinitionId;
    private boolean suspended;
    private String formKey;
    private String tenantId;

    public Task() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAssignee() {
        return assignee;
    }

    public String getCreated() {
        return created;
    }

    public String getDue() {
        return due;
    }

    public String getFollowUp() {
        return followUp;
    }

    public String getDelegationState() {
        return delegationState;
    }

    public String getDescription() {
        return description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getOwner() {
        return owner;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public int getPriority() {
        return priority;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public String getCaseExecutionId() {
        return caseExecutionId;
    }

    public String getCaseInstanceId() {
        return caseInstanceId;
    }

    public String getCaseDefinitionId() {
        return caseDefinitionId;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public String getFormKey() {
        return formKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            name,
            assignee,
            created,
            due,
            followUp,
            delegationState,
            description,
            executionId,
            owner,
            parentTaskId,
            priority,
            processDefinitionId,
            processInstanceId,
            taskDefinitionKey,
            caseExecutionId,
            caseInstanceId,
            caseDefinitionId,
            suspended,
            formKey,
            tenantId
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Task task = (Task) object;
        return Objects.equals(priority, task.priority)
               && Objects.equals(suspended, task.suspended);

    }

    @Override
    public String toString() {
        return "Task{"
               + "id='" + id + '\''
               + ", name='" + name + '\''
               + ", assignee='" + assignee + '\''
               + ", created='" + created + '\''
               + ", due='" + due + '\''
               + ", followUp='" + followUp + '\''
               + ", delegationState='" + delegationState + '\''
               + ", description='" + description + '\''
               + ", executionId='" + executionId + '\''
               + ", owner='" + owner + '\''
               + ", parentTaskId='" + parentTaskId + '\''
               + ", priority=" + priority
               + ", processDefinitionId='" + processDefinitionId + '\''
               + ", processInstanceId='" + processInstanceId + '\''
               + ", taskDefinitionKey='" + taskDefinitionKey + '\''
               + ", caseExecutionId='" + caseExecutionId + '\''
               + ", caseInstanceId='" + caseInstanceId + '\''
               + ", caseDefinitionId='" + caseDefinitionId + '\''
               + ", suspended=" + suspended
               + ", formKey='" + formKey + '\''
               + ", tenantId='" + tenantId + '\''
               + '}';
    }
}

