package ru.data.DTO.controller.school.analytics;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.school.analytics.PeriodsController;

/**
 * RU: Данные клиента используемые PeriodsController в методах
 *
 * @see PeriodsController
 */
@ToString
@RequiredArgsConstructor
public final class PeriodsInnerDTO {
    public final String name, perN, perK;
}
