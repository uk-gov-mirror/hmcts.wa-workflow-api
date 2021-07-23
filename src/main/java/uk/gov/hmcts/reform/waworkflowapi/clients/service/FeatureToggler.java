package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import uk.gov.hmcts.reform.waworkflowapi.config.features.FeatureFlag;

public interface FeatureToggler {

    boolean getValue(FeatureFlag featureFlag, Boolean defaultValue);

}
