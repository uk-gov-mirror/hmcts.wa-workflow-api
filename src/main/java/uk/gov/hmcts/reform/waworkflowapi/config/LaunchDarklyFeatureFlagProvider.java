package uk.gov.hmcts.reform.waworkflowapi.config;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class LaunchDarklyFeatureFlagProvider {

    private final LDClientInterface ldClient;

    public LaunchDarklyFeatureFlagProvider(LDClientInterface ldClient) {
        this.ldClient = ldClient;
    }

    public boolean getBooleanValue(FeatureFlag featureFlag) {
        requireNonNull(featureFlag, "featureFlag must not be null");
        log.info("Attempting to retrieve feature flag '{}' as Boolean", featureFlag.getKey());
        return ldClient.boolVariation(featureFlag.getKey(), createLaunchDarklyUser(), false);
    }

    private LDUser createLaunchDarklyUser() {
        return new LDUser.Builder("wa-workflow-api")
            .firstName("Work Allocation")
            .lastName("Workflow Api")
            .build();
    }
}
