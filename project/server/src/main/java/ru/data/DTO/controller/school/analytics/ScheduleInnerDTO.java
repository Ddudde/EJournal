package ru.data.DTO.controller.school.analytics;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.school.analytics.ScheduleController;

/**
 * RU: Данные клиента используемые ScheduleController в методах
 *
 * @see ScheduleController
 */
@ToString
@RequiredArgsConstructor
public final class ScheduleInnerDTO {
    public final JsonObject obj;
    public final Long group;
    public final int day;
}
