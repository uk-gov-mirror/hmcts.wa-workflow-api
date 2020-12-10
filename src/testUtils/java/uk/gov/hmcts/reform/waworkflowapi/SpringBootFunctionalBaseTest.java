package uk.gov.hmcts.reform.waworkflowapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = {"local","functional"})
public abstract class SpringBootFunctionalBaseTest {

    public static final String WA_TASK_INITIATION_IA_ASYLUM = "wa-task-initiation-ia-asylum";

    @Value("${targets.instance}")
    public String testUrl;

    @Value("${camunda.url}")
    public String camundaUrl;

}
