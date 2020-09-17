package uk.gov.hmcts.reform.waworkflowapi.duedate;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.waworkflowapi.duedate.holidaydates.HolidayService;
import uk.gov.hmcts.reform.waworkflowapi.external.taskservice.TaskToCreate;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

@Component
public class DueDateService {

    private final DateService dateService;
    private final HolidayService holidayService;

    public DueDateService(DateService dateService, HolidayService holidayService) {
        this.dateService = dateService;
        this.holidayService = holidayService;
    }

    public ZonedDateTime calculateDueDate(ZonedDateTime dueDate, TaskToCreate taskToCreate) {
        if (dueDate != null) {
            return dueDate;
        }
        int workingDaysAllowed = taskToCreate.getWorkingDaysAllowed();
        if (workingDaysAllowed == 0) {
            throw new IllegalStateException(
                "Should either have a due date or have got the working days allowed for task"
            );
        }
        return addWorkingDays(workingDaysAllowed);
    }

    public ZonedDateTime addWorkingDays(int numberOfDays) {
        return addWorkingDays(dateService.now(), numberOfDays);
    }

    private ZonedDateTime addWorkingDays(ZonedDateTime startDate, int numberOfDays) {
        if (numberOfDays == 0) {
            return startDate;
        }

        ZonedDateTime newDate = startDate.plusDays(1);
        if (isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDays(newDate, numberOfDays);
        } else {
            return addWorkingDays(newDate, numberOfDays - 1);
        }
    }

    private boolean isWeekend(ZonedDateTime date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
