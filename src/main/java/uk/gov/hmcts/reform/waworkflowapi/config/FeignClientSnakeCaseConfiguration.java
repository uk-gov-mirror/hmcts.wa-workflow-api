package uk.gov.hmcts.reform.waworkflowapi.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson3.Jackson3Decoder;
import feign.jackson3.Jackson3Encoder;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * Feign client configuration referenced only via {@code @FeignClient(configuration = ...)}.
 * Not annotated as Spring {@code @Configuration} to avoid becoming a parent context for all Feign clients.
 * Uses Jackson 3 JsonMapper with snake_case for task-management API.
 */
public class FeignClientSnakeCaseConfiguration {

    @Bean
    public Decoder feignDecoder() {
        return new Jackson3Decoder(snakeCaseJsonMapper());
    }

    @Bean
    public Encoder feignEncoder() {
        return new Jackson3Encoder(snakeCaseJsonMapper());
    }

    public JsonMapper snakeCaseJsonMapper() {
        return JsonMapper.builder()
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    }
}
