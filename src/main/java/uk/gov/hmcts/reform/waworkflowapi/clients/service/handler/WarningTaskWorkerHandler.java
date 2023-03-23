package uk.gov.hmcts.reform.waworkflowapi.clients.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.TaskManagementServiceApi;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.AddProcessVariableRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaTask;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class WarningTaskWorkerHandler {

    public static final String WARNING_LIST = "warningList";
    public static final String WARNINGS_TO_ADD = "warningsToAdd";
    final TaskManagementServiceApi taskManagementServiceApi;
    final AuthTokenGenerator authTokenGenerator;
    final LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider;
    private final CamundaClient camundaClient;

    public WarningTaskWorkerHandler(TaskManagementServiceApi taskManagementServiceApi,
                                    AuthTokenGenerator authTokenGenerator,
                                    LaunchDarklyFeatureFlagProvider launchDarklyFeatureFlagProvider,
                                    CamundaClient camundaClient) {
        this.taskManagementServiceApi = taskManagementServiceApi;
        this.authTokenGenerator = authTokenGenerator;
        this.launchDarklyFeatureFlagProvider = launchDarklyFeatureFlagProvider;
        this.camundaClient = camundaClient;
    }

    public void completeWarningTaskService(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        log.info("completeWarningTaskService received externalTask:{}", externalTask);
        Map<?, ?> variables = externalTask.getAllVariables();
        if (variables == null) {
            log.warn("completeWarningTaskService can NOT continue to process due to externalTask is null. "
                     + "externalTask:{}", externalTask);
            return;
        }
        String caseId = (String) variables.get("caseId");
        log.info("Set processVariables for same processInstance ids with caseId {}", caseId);

        String updatedWarningValues = "[]";

        try {
            updatedWarningValues = mapWarningValues(variables);
        } catch (JsonProcessingException exp) {
            log.error("Exception occurred while parsing json: {}", exp.getMessage(), exp);
        }

        externalTaskService.complete(externalTask, Map.of(
            "hasWarnings",
            true,
            WARNING_LIST,
            updatedWarningValues
        ));
        //Also update the warning in CFT Task DB
        String taskName = (String) variables.get("name");
        addWarningInCftTaskDb(caseId, updatedWarningValues, taskName);
        addWarningToDelayedProcesses(caseId, updatedWarningValues);

    }

    private void addWarningToDelayedProcesses(String caseId, String updatedWarningValues) {
        List<CamundaProcess> camundaProcessList = getProcesses(caseId);
        if (camundaProcessList == null) {
            log.warn("addWarningToDelayedProcesses can NOT continue to process due to camundaProcessList is null. "
                     + "caseId:{} updatedWarningValues:{}", caseId, updatedWarningValues);
            return;
        }

        camundaProcessList.forEach(process -> {
            if (process == null) {
                log.warn("addWarningToDelayedProcesses can NOT continue to process due to camundaProcess is null. "
                         + "caseId:{} updatedWarningValues:{}", caseId, updatedWarningValues);
            } else {
                updateDelayedProcessWarnings(caseId, process, updatedWarningValues);
            }
        });
    }

    private void updateDelayedProcessWarnings(String caseId, CamundaProcess process, String warningToAdd) {
        String serviceToken = authTokenGenerator.generate();
        CamundaProcessVariables processVariables = camundaClient.getProcessInstanceVariables(
            serviceToken,
            process.getId()
        );

        if (processVariables == null || processVariables.getProcessVariablesMap() == null) {
            log.warn("updateDelayedProcessWarnings processVariables not found. "
                     + "caseId:{} warningToAdd:{} tenantId:{} processId:{}",
                caseId, warningToAdd, process.getTenantId(), process.getId());
            return;
        } else {
            log.info("updateDelayedProcessWarnings "
                     + "caseId:{} warningToAdd:{} tenantId:{} processId:{} processVariables:{}",
                caseId, warningToAdd, process.getTenantId(), process.getId(), processVariables);
        }

        String warning = "[]";
        if (processVariables.getProcessVariablesMap().get(WARNING_LIST) != null) {
            warning = (String) processVariables.getProcessVariablesMap().get(WARNING_LIST).getValue();
        }

        LocalDateTime delayDate;
        try {
            delayDate = LocalDateTime.parse((String) processVariables.getProcessVariablesMap().get("delayUntil").getValue());
        } catch (Exception e) {
            log.warn(String.format("updateDelayedProcessWarnings delayUntil is null. processId:%s ", process.getId()), e);
            return;
        }

        if (delayDate.isAfter(LocalDateTime.now())) {
            try {
                WarningValues values = mapWarningAttributes(new WarningValues(warningToAdd), new WarningValues(warning));
                warning = values.getValuesAsJson();

            } catch (JsonProcessingException exp) {
                log.error("Exception occurred while parsing json: {}", exp.getMessage(), exp);
            }

            Map<String, DmnValue<String>> warningList = Map.of(WARNING_LIST, DmnValue.dmnStringValue(warning));
            AddProcessVariableRequest modificationRequest = new AddProcessVariableRequest(warningList);
            camundaClient.updateProcessVariables(
                serviceToken,
                process.getId(),
                modificationRequest
            );

        }
    }

    private void addWarningInCftTaskDb(String caseId, String updatedWarningValues, String taskName) {

        NotesRequest notesRequest = prepareNoteRequest(updatedWarningValues);
        try {
            getTasks(caseId).forEach(task -> {
                if (task.getName().equals(taskName)) {
                    taskManagementServiceApi.addTaskNote(authTokenGenerator.generate(), task.getId(), notesRequest);
                }
            });
        } catch (Exception e) {
            String message = String.format("Exception occurred while contacting taskManagement Api. "
                                           + "caseId:%s updatedWarningValues:%s taskName:%s notesRequest:%s",
                caseId, updatedWarningValues, taskName, notesRequest);
            log.error(message, e);
        }
    }

    private String mapWarningValues(Map<?, ?> variables) throws JsonProcessingException {
        WarningValues processVariableWarningValues = toWarningValues(variables, WARNING_LIST);
        WarningValues warningValuesToBeAdded = toWarningValues(variables, WARNINGS_TO_ADD);

        WarningValues combinedWarningValues = mapWarningAttributes(
            warningValuesToBeAdded,
            processVariableWarningValues
        );

        String caseId = (String) variables.get("caseId");
        log.info("caseId {} and its warning values : {}", caseId, combinedWarningValues.getValuesAsJson());

        return combinedWarningValues.getValuesAsJson();
    }

    private WarningValues toWarningValues(Map<?, ?> variables, String warningList) {
        final String warningStr = (String) variables.get(warningList);
        return new WarningValues(Objects.requireNonNullElse(warningStr, "[]"));
    }

    private WarningValues mapWarningAttributes(WarningValues warningsToAdd,
                                               WarningValues processVariableWarningTextValues) {

        // without duplicate warning attributes
        final List<Warning> warningTextValues = Stream.concat(
                warningsToAdd.getValues().stream(),
                processVariableWarningTextValues.getValues().stream()
            )
            .distinct().collect(Collectors.toList());
        return new WarningValues(warningTextValues);

    }

    private List<CamundaProcess> getProcesses(String caseId) {
        return camundaClient.getProcessInstancesByVariables(
            authTokenGenerator.generate(),
            "caseId_eq_" + caseId,
            List.of("processStartTimer")
        );
    }

    public List<CamundaTask> getTasks(String caseId) {

        return camundaClient.searchByCaseId(
            authTokenGenerator.generate(),
            Map.of(
                "processVariables", List.of(Map.of(
                    "name", "caseId",
                    "operator", "eq",
                    "value", caseId
                ))
            )
        );
    }

    private NotesRequest prepareNoteRequest(String warningValues) {
        List<Warning> warnings = new ArrayList<>();
        try {
            warnings = new ObjectMapper().reader()
                .forType(new TypeReference<List<Warning>>() {
                }).readValue(warningValues);
        } catch (JsonProcessingException e) {
            log.info("Couldn't map json");
        }

        return new NotesRequest(warnings
                                    .stream()
                                    .map(warning ->
                                             new NoteResource(warning.getWarningCode(),
                                                              "WARNING",
                                                              "some-user",
                                                              warning.getWarningText()))
                                    .collect(Collectors.toList()));
    }

}

