package uk.gov.hmcts.reform.waworkflowapi.config.features;

public enum FeatureFlag {

    //Features
    WA_NON_IAC_WARNINGS("wa-dmn-warnings-non-iac-feature");

    private final String key;

    FeatureFlag(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
