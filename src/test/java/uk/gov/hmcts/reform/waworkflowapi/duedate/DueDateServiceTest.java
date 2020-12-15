package uk.gov.hmcts.reform.waworkflowapi.duedate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.waworkflowapi.common.TaskToCreate;
import uk.gov.hmcts.reform.waworkflowapi.duedate.holidaydates.HolidayService;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
class DueDateServiceTest {

    public static final String TCW_GROUP = "TCW";
    private static final String NAME = "task name";
    private FixedDateService dateService;
    private DueDateService underTest;
    private HolidayService holidayService;

    @BeforeEach
    void setUp() {
        dateService = new FixedDateService();
        holidayService = mock(HolidayService.class);
        underTest = new DueDateService(dateService, holidayService);
    }

    @Test
    void haveToSetEitherADueDateOrHaveWorkingDays() {
        TaskToCreate taskToCreate = new TaskToCreate("processApplication", TCW_GROUP, NAME);
        assertThrows(IllegalStateException.class, () -> {
            underTest.calculateDueDate(
                null,
                taskToCreate
            );
        });
    }

    @Test
    void ifADueDateIsAlreadySetDoNotCalculateANewOne() {
        ZonedDateTime providedDueDate = ZonedDateTime.now();
        ZonedDateTime calculatedDueDate = underTest.calculateDueDate(
            providedDueDate,
            new TaskToCreate("processApplication", TCW_GROUP, NAME)
        );

        assertThat(calculatedDueDate, is(providedDueDate));
    }

    @Test
    void calculateDueDateAllWorkingDays() {
        checkWorkingDays(ZonedDateTime.of(2020, 9, 1, 1, 2, 3, 4, ZoneId.systemDefault()),
                         2, ZonedDateTime.of(2020, 9, 1, 1, 2, 3, 4, ZoneId.systemDefault()).plusDays(2)
        );
    }

    @Test
    void calculateDueDateWhenFallInAWeekend() {
        checkWorkingDays(ZonedDateTime.of(2020, 9, 3, 1, 2, 3, 4, ZoneId.systemDefault()), 2,
                         ZonedDateTime.of(2020, 9, 7, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    @Test
    void calculateDueDateWhenStraddlesAWeekend() {
        checkWorkingDays(ZonedDateTime.of(2020, 9, 3, 1, 2, 3, 4, ZoneId.systemDefault()), 4,
                         ZonedDateTime.of(2020, 9, 9, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    @Test
    void calculateDueDateWhichStraddlesMultipleWeekends() {
        checkWorkingDays(ZonedDateTime.of(2020, 9, 3, 1, 2, 3, 4, ZoneId.systemDefault()), 10,
                         ZonedDateTime.of(2020, 9, 17, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    @Test
    void calculateDueDateWhichFallsOnAWeekend() {
        checkWorkingDays(ZonedDateTime.of(2020, 9, 3, 1, 2, 3, 4, ZoneId.systemDefault()), 10,
                         ZonedDateTime.of(2020, 9, 17, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    @Test
    void calculateDueDateWhichFallsOnAHoliday() {
        when(holidayService.isHoliday(ZonedDateTime.of(2020, 9, 3, 1, 2, 3, 4, ZoneId.systemDefault())))
            .thenReturn(true);
        checkWorkingDays(ZonedDateTime.of(2020, 9, 1, 1, 2, 3, 4, ZoneId.systemDefault()), 2,
                         ZonedDateTime.of(2020, 9, 4, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    @Test
    void calculateDueDateWhichStraddlesAHoliday() {
        when(holidayService.isHoliday(ZonedDateTime.of(2020, 9, 1, 1, 2, 3, 4, ZoneId.systemDefault()).plusDays(1)))
            .thenReturn(true);
        checkWorkingDays(ZonedDateTime.of(2020, 9, 1, 1, 2, 3, 4, ZoneId.systemDefault()), 2,
                         ZonedDateTime.of(2020, 9, 4, 1, 2, 3, 4, ZoneId.systemDefault())
        );
    }

    private void checkWorkingDays(ZonedDateTime startDay, int leadTimeDays, ZonedDateTime expectedDueDate) {
        dateService.setCurrentDateTime(startDay);

        ZonedDateTime calculatedDueDate = underTest.calculateDueDate(
            null,
            new TaskToCreate("processApplication", TCW_GROUP, leadTimeDays, NAME)
        );

        assertThat(calculatedDueDate, is(expectedDueDate));
    }

}
