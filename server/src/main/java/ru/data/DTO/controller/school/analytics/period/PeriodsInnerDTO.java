package ru.data.DTO.controller.school.analytics.period;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.school.analytics.PeriodController;

/**
 * RU: Данные клиента используемые PeriodsController в методах
 *
 * @see PeriodController
 */
@ToString
@RequiredArgsConstructor
public final class PeriodsInnerDTO {
    public final String name, perN, perK;
}
