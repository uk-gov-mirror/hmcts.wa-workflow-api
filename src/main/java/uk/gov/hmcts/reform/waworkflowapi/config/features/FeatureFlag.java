package uk.gov.hmcts.reform.waworkflowapi.config.features;

public enum FeatureFlag {

    //Features
    RELEASE_2_CFT_TASK_WARNING("wa-release-2-cft-task-warning"),

    //The following keys are used for testing purposes only.
    TEST_KEY("tester"),
    NON_EXISTENT_KEY("non-existent");

    private final String key;

    FeatureFlag(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
