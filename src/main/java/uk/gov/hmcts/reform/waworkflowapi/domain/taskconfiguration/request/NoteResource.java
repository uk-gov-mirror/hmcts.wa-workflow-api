package uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class NoteResource {

    private final String code;
    private final String noteType;
    private final String userId;
    private final String content;

    public NoteResource(String code,
                        String noteType,
                        String userId,
                        String content) {
        this.code = code;
        this.noteType = noteType;
        this.userId = userId;
        this.content = content;
    }

    public String getCode() {
        return code;
    }

    public String getNoteType() {
        return noteType;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}
