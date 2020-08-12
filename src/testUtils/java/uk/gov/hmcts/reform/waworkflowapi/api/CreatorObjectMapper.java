package uk.gov.hmcts.reform.waworkflowapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CreatorObjectMapper {
    private CreatorObjectMapper() {
    }

    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
