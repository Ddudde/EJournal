package ru.services.logic.school;

import com.google.gson.Gson;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.configs.AppConfig;
import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.services.logic.school.analytics.PeriodService;
import utils.TestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PeriodServiceTest {
    protected static final Gson gson = new Gson();
    private final TestUtils testUtils = new TestUtils();

    @Spy
    private AppConfig config = new AppConfig();

    @InjectMocks
    private PeriodService periodService;

    /** RU: общий сценарий тестирования */
    private Period getActualPeriodBySchool_run(School school, LocalDate date) {
        when(school.getPeriods()).thenReturn(testUtils.periods);
        try (MockedStatic<LocalDate> mocked = Mockito.mockStatic(LocalDate.class)) {
            mocked.when(LocalDate::now).thenReturn(date);
            return periodService.getActualPeriodBySchool(school);
        }
    }

    /** RU: имитирует 25 марта 2023 и должен вернуть NULL */
    @Test
    @Tag("getActualPeriodBySchool")
    void getActualPeriodBySchool_whenBad(@Mock School school) {
        LocalDate date = LocalDate.of(2023, 3, 25);
        assertNull(getActualPeriodBySchool_run(school, date));
    }

    /** RU: имитирует 25 марта 2024 и должен выбрать актуальной 3 четверть */
    @Test @Tag("getActualPeriodBySchool")
    void getActualPeriodBySchool_whenGood(@Mock School school) {
        LocalDate date = LocalDate.of(2024, 3, 25);
        assertEquals("III четверть",
            getActualPeriodBySchool_run(school, date).getName());
    }
}