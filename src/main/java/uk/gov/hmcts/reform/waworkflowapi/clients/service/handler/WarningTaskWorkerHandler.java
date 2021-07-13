package uk.gov.hmcts.reform.waworkflowapi.clients.service.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Warning;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.WarningValues;

import java.util.Map;

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
        WarningValues warningValues;
        if (warningStr == null) {
            warningValues = new WarningValues("[]");
        } else {
            warningValues = new WarningValues(warningStr);
        }

        final Warning warning = mapWarningAttributes(variables);
        if (warning != null) {
            warningValues.getValues().add(warning);
        }

        String caseId = (String) variables.get("caseId");
        log.info("caseId {} and its warning values : {}", caseId, warningValues.getValuesAsJson());
        return warningValues.getValuesAsJson();
    }

    private Warning mapWarningAttributes(Map<?, ?> variables) {
        final String warningCode = (String) variables.get("warningCode");
        final String warningText = (String) variables.get("warningText");

        if (warningCode != null && warningText != null) {
            return new Warning(warningCode, warningText);
        }
        return null;
    }

}

