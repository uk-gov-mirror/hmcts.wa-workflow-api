package uk.gov.hmcts.reform.waworkflowapi.exceptions.enums;

public enum ErrorMessages {

    GENERIC_FORBIDDEN_ERROR(
        "The action could not be completed because the client/user had insufficient rights to a resource."),

    EVALUATE_DMN_ERROR(
        "The action could not be completed because there was a problem when evaluating DMN."),

    UNSUPPORTED_MEDIA_TYPE(
        "The action could not be completed because Unsupported media type only application/json requests are supported."),

    DOWNSTREAM_DEPENDENCY_ERROR(
        "Downstream dependency did not respond as expected and the request could not be completed.");

    private final String detail;

    ErrorMessages(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

}
