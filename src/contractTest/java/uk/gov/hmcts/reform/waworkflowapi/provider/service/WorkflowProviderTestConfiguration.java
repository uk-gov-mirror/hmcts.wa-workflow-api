package uk.gov.hmcts.reform.waworkflowapi.provider.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.CamundaClient;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.TaskClientService;

@TestConfiguration
public class WorkflowProviderTestConfiguration {

    @MockBean
    private CamundaClient camundaClient;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Bean
    @Primary
    public TaskClientService taskClientService() {
        return new TaskClientService(
            camundaClient,
            authTokenGenerator
        );
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
            .serializationInclusion(JsonInclude.Include.NON_ABSENT)
            .propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .modules(
                new ParameterNamesModule(),
                new JavaTimeModule(),
                new Jdk8Module()
            );
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }
}
