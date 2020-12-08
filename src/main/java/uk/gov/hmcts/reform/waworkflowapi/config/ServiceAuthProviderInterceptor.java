package uk.gov.hmcts.reform.waworkflowapi.config;

import org.camunda.bpm.client.interceptor.ClientRequestContext;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.reform.waworkflowapi.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@Component
public class ServiceAuthProviderInterceptor implements ClientRequestInterceptor {
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    public ServiceAuthProviderInterceptor(AuthTokenGenerator serviceAuthTokenGenerator) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
    }

    @Override
    public void intercept(ClientRequestContext requestContext) {
        requestContext.addHeader(SERVICE_AUTHORIZATION, serviceAuthTokenGenerator.generate());
    }

}
