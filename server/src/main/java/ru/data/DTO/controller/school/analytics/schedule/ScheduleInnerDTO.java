package ru.data.DTO.controller.school.analytics.schedule;

import ru.controllers.school.analytics.ScheduleController;

/**
 * RU: Данные клиента используемые ScheduleController в методах
 *
 * @see ScheduleController
 */
public record ScheduleInnerDTO(ScheduleInnerBodyDTO obj, Long group, int day) {
}
