package ru.data.DTO.controller.people.teacher;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.controllers.people.TeachersController;

/**
 * RU: Данные клиента используемые TeachersController в методах
 *
 * @see TeachersController
 */
@ToString
@RequiredArgsConstructor
public final class TeachersInnerDTO {
    public final String name;
    public final Long id;
}
