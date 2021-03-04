package uk.gov.hmcts.reform.waworkflowapi;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.waworkflowapi.common.LaunchDarklyClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LaunchDarklyFunctionalTest extends SpringBootFunctionalBaseTest {

    @Autowired
    private LaunchDarklyClient launchDarklyClient;



    @Test
    public void should_hit_launch_darkly() {
        boolean launchDarklyFeature = launchDarklyClient.getKey("tester");

        assertThat(launchDarklyFeature, is(true));
    }
}
