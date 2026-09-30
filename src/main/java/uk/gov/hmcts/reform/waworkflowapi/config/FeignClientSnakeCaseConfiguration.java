package uk.gov.hmcts.reform.waworkflowapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.context.annotation.Bean;

/**
 * Feign client configuration referenced only via {@code @FeignClient(configuration = ...)}.
 * Not annotated as Spring {@code @Configuration} to avoid becoming a parent context for all Feign clients.
 * Uses Jackson 2 ObjectMapper with snake_case for task-management API.
 */
public class FeignClientSnakeCaseConfiguration {
    @Bean
    public Decoder feignDecoder() {
        return new JacksonDecoder(snakeCaseObjectMapper());
    }

    @Bean
    public Encoder feignEncoder() {
        return new JacksonEncoder(snakeCaseObjectMapper());
    }

    public ObjectMapper snakeCaseObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
