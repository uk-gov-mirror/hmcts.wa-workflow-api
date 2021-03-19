package uk.gov.hmcts.reform.waworkflowapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.idempotencykey.IdempotencyKeys;

@Configuration
public class IdempotentKeysRepositoryConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(IdempotencyKeys.class);
    }

}
