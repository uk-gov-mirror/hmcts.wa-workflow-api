package uk.gov.hmcts.reform.waworkflowapi.external.taskservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SendMessageService {
    private final TaskClientService taskClientService;

    @Autowired
    public SendMessageService(TaskClientService taskClientService) {
        this.taskClientService = taskClientService;
    }

    public void createMessage(SendMessageRequest sendMessageRequest) {
        taskClientService.sendMessage(sendMessageRequest);
    }

}
