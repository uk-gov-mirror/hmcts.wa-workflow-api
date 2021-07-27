package uk.gov.hmcts.reform.waworkflowapi.clients.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class WarningTaskWorkerHandler {

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
            "warningList",
            updatedWarningValues
        ));

    }

    private String mapWarningValues(Map<?, ?> variables) throws JsonProcessingException {
        final String warningStr = (String) variables.get("warningList");
        WarningValues processVariableWarningValues;
        if (warningStr == null) {
            processVariableWarningValues = new WarningValues("[]");
        } else {
            processVariableWarningValues = new WarningValues(warningStr);
        }

        WarningValues combinedWarningValues = mapWarningAttributes(variables, processVariableWarningValues);

        String caseId = (String) variables.get("caseId");
        log.info("caseId {} and its warning values : {}", caseId, combinedWarningValues.getValuesAsJson());

        return combinedWarningValues.getValuesAsJson();
    }

    private WarningValues mapWarningAttributes(Map<?, ?> variables, WarningValues processVariableWarningTextValues) {
        final String warningsToAddAsJson = (String) variables.get("warningsToAdd");

        if (!StringUtils.isEmpty(warningsToAddAsJson)) {
            final WarningValues warningValues = new WarningValues(warningsToAddAsJson);
            final List<Warning> warningsToBeAdded = warningValues.getValues();

            final List<Warning> processVariableWarnings = processVariableWarningTextValues.getValues();

            // without duplicate warning attributes
            final List<Warning> warningTextValues = Stream.concat(warningsToBeAdded.stream(), processVariableWarnings.stream())
                .distinct().collect(Collectors.toList());
            return new WarningValues(warningTextValues);
        }
        return processVariableWarningTextValues;
    }

}

