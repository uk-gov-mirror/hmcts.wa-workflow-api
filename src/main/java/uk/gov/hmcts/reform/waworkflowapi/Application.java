package uk.gov.hmcts.reform.waworkflowapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentId;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotentkey.IdempotentKeys;
import uk.gov.hmcts.reform.waworkflowapi.clients.service.IdempotentKeysRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootApplication
@EnableCircuitBreaker
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.waworkflowapi",
    })
@Slf4j
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner demo(IdempotentKeysRepository repository) {
        return args -> {
            IdempotentKeys idempotentKeys = new IdempotentKeys(
                new IdempotentId(UUID.randomUUID().toString(), "ia"),
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            repository.save(idempotentKeys);

        };
    }

}
