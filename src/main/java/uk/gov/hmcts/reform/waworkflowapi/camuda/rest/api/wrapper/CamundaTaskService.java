package uk.gov.hmcts.reform.waworkflowapi.camuda.rest.api.wrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings({"PMD.AvoidPrintStackTrace","PMD.AvoidThrowingNewInstanceOfSameException","PMD.PreserveStackTrace"})
@Service
public class CamundaTaskService {

    private final CamundaTaskServiceWrapper camundaTaskServiceWrapper;

    @Autowired
    public CamundaTaskService(CamundaTaskServiceWrapper camundaTaskServiceWrapper) {
        this.camundaTaskServiceWrapper = camundaTaskServiceWrapper;
    }

    public String getTask(String id) {
        return camundaTaskServiceWrapper.getTask(id);
    }


}
