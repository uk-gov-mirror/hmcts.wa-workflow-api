package uk.gov.hmcts.reform.waworkflowapi.provider.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.TaskClientService;

@TestConfiguration
public class WorkflowProviderTestConfiguration {

    @MockitoBean
    private CamundaClient camundaClient;

    @MockitoBean
    AuthTokenGenerator authTokenGenerator;

    @Bean
    @Primary
    public TaskClientService taskClientService() {
        return new TaskClientService(
            camundaClient,
            authTokenGenerator
        );
    }

    /**
     * Jackson 2 ObjectMapper for provider MockMvc verification while Feign/clients remain on Jackson 2.
     * Prefer Jackson 3 once provider and Feign paths are fully ported.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        return objectMapper;
    }
}
