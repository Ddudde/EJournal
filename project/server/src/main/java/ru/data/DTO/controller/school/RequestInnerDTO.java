package ru.data.DTO.controller.school;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.school.RequestController;

/**
 * RU: Данные клиента используемые RequestController в методах
 *
 * @see RequestController
 */
@ToString
@RequiredArgsConstructor
public final class RequestInnerDTO {
    public final String text, date, title, email, fio;
    public final Long id;
}
