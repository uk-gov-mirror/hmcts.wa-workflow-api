package uk.gov.hmcts.reform.waworkflowapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCircuitBreaker
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableFeignClients(basePackages =
    {
        "uk.gov.hmcts.reform.auth",
        "uk.gov.hmcts.reform.authorisation",
        "uk.gov.hmcts.reform.waworkflowapi",
    })
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
