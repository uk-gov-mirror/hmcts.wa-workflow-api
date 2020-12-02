package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Map;

@Component
public class TaskClientService {
    private final CamundaClient camundaClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Autowired
    public TaskClientService(@Autowired CamundaClient camundaClient,
                             AuthTokenGenerator authTokenGenerator) {
        this.camundaClient = camundaClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    public void sendMessage(SendMessageRequest sendMessageRequest) {
        camundaClient.sendMessage(
            authTokenGenerator.generate(),
            sendMessageRequest
        );
    }

    public List<Map<String,DmnValue>> evaluate(EvaluateDmnRequest evaluateDmnRequest, String id) {
        String jurisdiction = evaluateDmnRequest.getServiceDetails().getJurisdiction();
        String caseType = evaluateDmnRequest.getServiceDetails().getCaseType();
        return camundaClient.evaluateDmn(
            authTokenGenerator.generate(),
            jurisdiction,
            caseType,
            evaluateDmnRequest
        );
    }


}
