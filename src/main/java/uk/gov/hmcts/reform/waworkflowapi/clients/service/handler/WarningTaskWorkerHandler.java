package uk.gov.hmcts.reform.waworkflowapi.clients.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.TaskManagementServiceApi;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.AddProcessVariableRequest;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcess;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.CamundaProcessVariables;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.DmnValue;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.config.LaunchDarklyFeatureFlagProvider;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NoteResource;
import uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request.NotesRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag.RELEASE_2_CFT_TASK_WARNING;

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
        Map<?, ?> variables = externalTask.getAllVariables();
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

        boolean isCftTaskWarningEnabled = launchDarklyFeatureFlagProvider.getBooleanValue(RELEASE_2_CFT_TASK_WARNING);
        if (isCftTaskWarningEnabled) {
            //Also update the warning in CFT Task DB
            addWarningInCftTaskDb(externalTask.getId());
            addWarningToDelayedProcesses(caseId, updatedWarningValues);
        }

    }

    private void addWarningToDelayedProcesses(String caseId, String updatedWarningValues) {
        List<CamundaProcess> processes = getProcesses(caseId);
        processes.forEach(process -> updateDelayedProcessWarnings(process, updatedWarningValues));
    }

    private void updateDelayedProcessWarnings(CamundaProcess process, String warningToAdd) {
        String serviceToken = authTokenGenerator.generate();
        CamundaProcessVariables processVariables = camundaClient.getProcessInstanceVariables(
            serviceToken,
            process.getId()
        );

        String warning = (String) processVariables.getProcessVariablesMap().get(WARNING_LIST).getValue();

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

    private void addWarningInCftTaskDb(String taskId) {
        NotesRequest notesRequest = new NotesRequest(
            singletonList(
                new NoteResource(null, "WARNING", null, null)
            )
        );

        taskManagementServiceApi.addTaskNote(authTokenGenerator.generate(), taskId, notesRequest);
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

}

