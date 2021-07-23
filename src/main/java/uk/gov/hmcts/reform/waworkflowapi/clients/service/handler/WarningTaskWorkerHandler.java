package uk.gov.hmcts.reform.waworkflowapi.clients.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.LaunchDarklyFeatureToggler;
import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class WarningTaskWorkerHandler {

    private final LaunchDarklyFeatureToggler featureToggler;

    public WarningTaskWorkerHandler(LaunchDarklyFeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
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

        WarningValues combinedWarningValues;
        final boolean nonIacWarningFeature = featureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false);
        if (nonIacWarningFeature) {
            combinedWarningValues = mapNonIacWaringAttributes(variables, processVariableWarningValues);
        } else {
            combinedWarningValues = mapWarningAttributes(variables, processVariableWarningValues);
        }

        String caseId = (String) variables.get("caseId");
        log.info("caseId {} and its warning values : {}", caseId, combinedWarningValues.getValuesAsJson());

        return combinedWarningValues.getValuesAsJson();
    }

    private WarningValues mapWarningAttributes(Map<?, ?> variables, WarningValues processVariableWarningValues) {
        final String warningCode = (String) variables.get("warningCode");
        final String warningText = (String) variables.get("warningText");

        if (warningCode != null && warningText != null) {
            processVariableWarningValues.getValues().add(new Warning(warningCode, warningText));
        }
        return processVariableWarningValues;
    }

    private WarningValues mapNonIacWaringAttributes(Map<?, ?> variables, WarningValues processVariableWarningValues) {
        final String warningsAsJson = (String) variables.get("warnings");

        if (!StringUtils.isEmpty(warningsAsJson)) {
            final WarningValues warningValues = new WarningValues(warningsAsJson);
            final List<Warning> handlerWarnings = warningValues.getValues();

            final List<Warning> processVariableWarnings = processVariableWarningValues.getValues();

            // without duplicate warning attributes
            final List<Warning> distinctWarnings = Stream.concat(handlerWarnings.stream(), processVariableWarnings.stream())
                .distinct().collect(Collectors.toList());
            return new WarningValues(distinctWarnings);
        }
        return processVariableWarningValues;
    }

}

