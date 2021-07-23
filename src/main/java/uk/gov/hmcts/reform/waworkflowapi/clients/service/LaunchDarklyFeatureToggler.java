package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

@Service
public class LaunchDarklyFeatureToggler implements FeatureToggler {
    private final LDClientInterface ldClient;

    public LaunchDarklyFeatureToggler(LDClientInterface ldClient) {
        this.ldClient = ldClient;
    }

    @Override
    public boolean getValue(FeatureFlag featureFlag, Boolean defaultValue) {

        return ldClient.boolVariation(
            featureFlag.getKey(),
            null,
            defaultValue
        );
    }

}
