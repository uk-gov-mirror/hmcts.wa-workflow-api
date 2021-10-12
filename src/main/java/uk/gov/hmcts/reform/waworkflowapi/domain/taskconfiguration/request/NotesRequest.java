package uk.gov.hmcts.reform.waworkflowapi.domain.taskconfiguration.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class NotesRequest {

    private final List<NoteResource> noteResource;

    @JsonCreator
    public NotesRequest(@JsonProperty("notes") List<NoteResource> noteResource) {
        this.noteResource = noteResource;
    }

    public List<NoteResource> getNoteResource() {
        return noteResource;
    }
}
