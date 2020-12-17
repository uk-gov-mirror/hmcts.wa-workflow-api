package uk.gov.hmcts.reform.waworkflowapi.clients.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue.dmnStringValue;

@EqualsAndHashCode
@ToString
public class ProcessVariables {
    private final DmnValue<String> jurisdiction;
    private final DmnValue<String> caseType;
    private final DmnValue<String> caseId;
    private final DmnValue<String> taskId;
    private final DmnValue<String> group;
    private final DmnValue<String> dueDate;
    private final DmnValue<String> name;

    public ProcessVariables(
        String jurisdiction,
        String caseType,
        String caseId,
        String taskId,
        String group,
        ZonedDateTime dueDate,
        String name
    ) {
        this.jurisdiction = dmnStringValue(jurisdiction);
        this.caseType = dmnStringValue(caseType);
        this.caseId = dmnStringValue(caseId);
        this.taskId = dmnStringValue(taskId);
        this.group = dmnStringValue(group);
        this.dueDate = dmnStringValue(dueDate.format(DateTimeFormatter.ISO_INSTANT));
        this.name = dmnStringValue(name);
    }

    public DmnValue<String> getJurisdiction() {
        return jurisdiction;
    }

    public DmnValue<String> getCaseType() {
        return caseType;
    }

    public DmnValue<String> getCaseId() {
        return caseId;
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

    public DmnValue<String> getName() {
        return name;
    }
}
