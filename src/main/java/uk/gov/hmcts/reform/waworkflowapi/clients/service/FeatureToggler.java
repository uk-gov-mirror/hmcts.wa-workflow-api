package uk.gov.hmcts.reform.waworkflowapi.clients.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
