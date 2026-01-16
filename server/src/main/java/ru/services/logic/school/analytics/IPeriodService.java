package ru.services.logic.school.analytics;

import ru.data.DAO.school.Period;
import ru.data.DAO.school.School;
import ru.data.DTO.controller.school.analytics.period.PeriodOutDTO;
import ru.data.DTO.controller.school.analytics.period.PeriodsInnerDTO;

import java.util.List;

public interface IPeriodService {
    PeriodOutDTO addPeriod(PeriodsInnerDTO body, School school);

    PeriodOutDTO prepareInfo(List<Period> periods);

    Period getActualPeriodBySchool(School school);
}
