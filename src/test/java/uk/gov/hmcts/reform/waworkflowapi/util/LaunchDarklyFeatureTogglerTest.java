package uk.gov.hmcts.reform.waworkflowapi.util;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.LaunchDarklyFeatureToggler;
import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchDarklyFeatureTogglerTest {

    @Mock
    private LDClientInterface ldClient;

    @InjectMocks
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    @Test
    void should_return_default_value_when_key_does_not_exist() {
        when(ldClient.boolVariation(
            FeatureFlag.WA_NON_IAC_WARNINGS.getKey(),
            null,
            true)
        ).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, true));
    }

    @Test
    void should_return_value_when_key_exists() {
        when(ldClient.boolVariation(
            FeatureFlag.WA_NON_IAC_WARNINGS.getKey(),
            null,
            false)
        ).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(FeatureFlag.WA_NON_IAC_WARNINGS, false));
    }
}
