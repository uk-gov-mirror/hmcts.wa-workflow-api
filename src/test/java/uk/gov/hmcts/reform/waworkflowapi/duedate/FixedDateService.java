package uk.gov.hmcts.reform.waworkflowapi.duedate;

import java.time.ZonedDateTime;

public class FixedDateService implements DateService {

    private ZonedDateTime currentDateTime;

    public void setCurrentDateTime(ZonedDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    @Override
    public ZonedDateTime now() {
        return currentDateTime;
    }
}
