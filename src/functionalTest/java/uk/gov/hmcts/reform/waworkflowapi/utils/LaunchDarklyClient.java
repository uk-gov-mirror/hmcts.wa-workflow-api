package uk.gov.hmcts.reform.waworkflowapi.utils;


import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LaunchDarklyClient {

    private final LDClientInterface ldClient;

    @Autowired
    public LaunchDarklyClient(LDClientInterface ldClient) {
        this.ldClient = ldClient;
    }

    public boolean getKey(String key) {

        LDContext ldContext = LDContext.builder("wa-workflow-api")
            .build();

        return ldClient.boolVariation(key, ldContext, false);
    }
}
